package untis

import config.TimeTableConfig
import kotlinx.datetime.toKotlinLocalTime
import org.bytedream.untis4j.UntisUtils.LessonCode
import org.bytedream.untis4j.responseObjects.Timetable.Lesson
import utils.getLogger
import utils.ifFalse
import java.time.Duration

class LessonParser(val config: TimeTableConfig, val doubleLessonMaxBreakMinutes: Int) {
    val logger = getLogger()

    fun parseChanges(lessons: Iterable<Lesson>): List<LessonChange> = lessons
        .groupBy { it.date to it.startTime }
        .flatMap { (_, slot) -> parseSlot(slot) }
        .sortedWith(compareBy({ it.date }, { it.lessonTimes.first }))
        .fold(mutableListOf()) { merged, change ->
            val index = merged.indexOfLast { it.canMergeWith(change) }
            if (index == -1) {
                merged += change
            } else {
                merged[index] = merged[index].let {
                    it.copy(
                        lessonTimes = it.lessonTimes.first..change.lessonTimes.last,
                        endTime = change.endTime
                    )
                }
                logger.debug(
                    "merged double lesson ({}, {}, {})",
                    change.date,
                    merged[index].lessonTimeLabel,
                    change.lessonName
                )
            }
            merged
        }

    private fun LessonChange.canMergeWith(next: LessonChange) =
        date == next.date &&
                type == next.type &&
                lessonName == next.lessonName &&
                change == next.change &&
                lessonTimes.last + 1 == next.lessonTimes.first && // => next lesson is the next lesson in the timetable
                !Duration.between(endTime, next.startTime).isNegative && // => next lesson starts after this lesson ends
                Duration.between(endTime, next.startTime).toMinutes() <= doubleLessonMaxBreakMinutes // => next lesson starts within the allowed break time

    private fun parseSlot(lessons: List<Lesson>): List<LessonChange> {
        val cancelled = lessons.filter { it.code == LessonCode.CANCELLED }.toMutableList()
        val changes = mutableListOf<LessonChange>()

        for (lesson in lessons.filter { it.code != LessonCode.CANCELLED }) {
            val replaced = cancelled.takeIf { lesson.code == LessonCode.IRREGULAR }?.firstOrNull()
            if (replaced != null) {
                cancelled -= replaced
                if (replaced.subjectName != lesson.subjectName) {
                    changes += change(replaced, LessonChangeType.SUBJECT, lesson.subjectName) ?: continue
                    logger.debug("found replaced lesson ({}, {})", replaced.startTime, replaced.subjectName)
                    continue
                }
            }
            changes += parseChange(lesson, isReplacement = replaced != null)
        }

        cancelled.forEach { lesson ->
            changes += change(lesson, LessonChangeType.CANCELLED, null) ?: return@forEach
            logger.debug("found cancelled lesson ({}, {})", lesson.startTime, lesson.subjectName)
        }

        return changes
    }

    private fun parseChange(lesson: Lesson, isReplacement: Boolean): List<LessonChange> {
        logger.debug("parsing lesson ({}) at ({})", lesson.subjectName, lesson.startTime)
        val changes = mutableListOf<LessonChange>()

        (lesson.originalSubjects.isEmpty()).ifFalse {
            changes += change(
                lesson,
                LessonChangeType.SUBJECT,
                lesson.subjectName,
                lesson.originalSubjects.firstOrNull()?.longName ?: lesson.subjectName
            ) ?: return changes
            logger.debug("found changed subject ({}, {})", lesson.startTime, lesson.subjectName)
        }

        (lesson.originalTeachers.isEmpty()).ifFalse {
            changes += change(lesson, LessonChangeType.TEACHER, lesson.teachers.firstOrNull()?.longName ?: "---")
                ?: return changes
            logger.debug("found changed teacher ({}, {})", lesson.startTime, lesson.subjectName)
        }

        (lesson.originalRooms.isEmpty()).ifFalse {
            changes += change(lesson, LessonChangeType.ROOM, lesson.rooms.firstOrNull()?.name ?: "---")
                ?: return changes
            logger.debug("found changed room ({}, {})", lesson.startTime, lesson.subjectName)
        }

        if (lesson.code == LessonCode.IRREGULAR && changes.isEmpty() && !isReplacement) {
            changes += change(lesson, LessonChangeType.ADDITIONAL, null) ?: return changes
            logger.debug("found additional lesson ({}, {})", lesson.startTime, lesson.subjectName)
        }

        return changes
    }

    private fun change(
        lesson: Lesson,
        type: LessonChangeType,
        change: String?,
        name: String = lesson.subjectName
    ): LessonChange? {
        val time = config[lesson.startTime.toKotlinLocalTime()] ?: run {
            logger.warn("invalid lesson time (${lesson.startTime})")
            return null
        }
        return LessonChange(type, lesson.date, time..time, name, change, lesson.startTime, lesson.endTime)
    }

    private val Lesson.subjectName: String
        get() = subjects.firstOrNull()?.longName ?: "---"
}
