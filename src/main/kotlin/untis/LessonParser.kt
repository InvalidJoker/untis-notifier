package untis

import config.TimeTableConfig
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalTime
import org.bytedream.untis4j.UntisUtils.LessonCode
import org.bytedream.untis4j.responseObjects.Timetable.Lesson
import utils.getLogger
import utils.ifFalse
import java.time.Duration

class LessonParser(val config: TimeTableConfig, val doubleLessonMaxBreakMinutes: Int) {
    val logger = getLogger()

    fun parseChanges(lessons: Iterable<Lesson>): List<LessonChange> = lessons
        .flatMap { it.split() }
        .groupBy { it.lesson.date to it.startTime }
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

    private fun Lesson.split(): List<LessonPart> {
        val starts = config.keys
            .map(LocalTime::toJavaLocalTime)
            .filter { startTime <= it && endTime > it }
            .sorted()
            .ifEmpty { return listOf(LessonPart(startTime, endTime, this)) }
        return starts.mapIndexed { index, start ->
            LessonPart(start, starts.getOrNull(index + 1) ?: endTime, this)
        }
    }

    private fun LessonChange.canMergeWith(next: LessonChange) =
        date == next.date &&
                type == next.type &&
                lessonName == next.lessonName &&
                change == next.change &&
                lessonTimes.last + 1 == next.lessonTimes.first && // => next lesson is the next lesson in the timetable
                !Duration.between(endTime, next.startTime).isNegative && // => next lesson starts after this lesson ends
                Duration.between(endTime, next.startTime).toMinutes() <= doubleLessonMaxBreakMinutes // => next lesson starts within the allowed break time

    private fun parseSlot(parts: List<LessonPart>): List<LessonChange> {
        val cancelled = parts.filter { it.lesson.code == LessonCode.CANCELLED }.toMutableList()
        val changes = mutableListOf<LessonChange>()

        for (part in parts.filter { it.lesson.code != LessonCode.CANCELLED }) {
            val replaced = cancelled.takeIf { part.lesson.code == LessonCode.IRREGULAR }?.firstOrNull()
            if (replaced != null) {
                cancelled -= replaced
                if (replaced.lesson.subjectName != part.lesson.subjectName) {
                    changes += change(replaced, LessonChangeType.SUBJECT, part.lesson.subjectName) ?: continue
                    logger.debug("found replaced lesson ({}, {})", replaced.startTime, replaced.lesson.subjectName)
                    continue
                }
            }
            changes += parseChange(part, isReplacement = replaced != null)
        }

        cancelled.forEach { part ->
            changes += change(part, LessonChangeType.CANCELLED, null) ?: return@forEach
            logger.debug("found cancelled lesson ({}, {})", part.startTime, part.lesson.subjectName)
        }

        return changes
    }

    private fun parseChange(part: LessonPart, isReplacement: Boolean): List<LessonChange> {
        val lesson = part.lesson
        logger.debug("parsing lesson ({}) at ({})", lesson.subjectName, part.startTime)
        val changes = mutableListOf<LessonChange>()

        (lesson.originalSubjects.isEmpty()).ifFalse {
            changes += change(
                part,
                LessonChangeType.SUBJECT,
                lesson.subjectName,
                lesson.originalSubjects.firstOrNull()?.longName ?: lesson.subjectName
            ) ?: return changes
            logger.debug("found changed subject ({}, {})", part.startTime, lesson.subjectName)
        }

        (lesson.originalTeachers.isEmpty()).ifFalse {
            changes += change(part, LessonChangeType.TEACHER, lesson.teachers.firstOrNull()?.longName ?: "---")
                ?: return changes
            logger.debug("found changed teacher ({}, {})", part.startTime, lesson.subjectName)
        }

        (lesson.originalRooms.isEmpty()).ifFalse {
            changes += change(part, LessonChangeType.ROOM, lesson.rooms.firstOrNull()?.name ?: "---")
                ?: return changes
            logger.debug("found changed room ({}, {})", part.startTime, lesson.subjectName)
        }

        if (lesson.code == LessonCode.IRREGULAR && changes.isEmpty() && !isReplacement) {
            changes += change(part, LessonChangeType.ADDITIONAL, null) ?: return changes
            logger.debug("found additional lesson ({}, {})", part.startTime, lesson.subjectName)
        }

        return changes
    }

    private fun change(
        part: LessonPart,
        type: LessonChangeType,
        change: String?,
        name: String = part.lesson.subjectName
    ): LessonChange? {
        val time = config[part.startTime.toKotlinLocalTime()] ?: run {
            logger.warn("invalid lesson time (${part.startTime})")
            return null
        }
        return LessonChange(type, part.lesson.date, time..time, name, change, part.startTime, part.endTime)
    }

    private val Lesson.subjectName: String
        get() = subjects.firstOrNull()?.longName ?: "---"
}
