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
        .flatMap { parseChange(it).orEmpty() }
        .sortedWith(compareBy({ it.date }, { it.lessonTimes.first }))
        .fold(mutableListOf()) { merged, change ->
            val index = merged.indexOfLast { it.canMergeWith(change) }
            if (index == -1) {
                merged += change
            } else {
                merged[index] = merged[index].let { it.copy(lessonTimes = it.lessonTimes.first..change.lessonTimes.last, endTime = change.endTime) }
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
            lessonTimes.last + 1 == next.lessonTimes.first &&
            !Duration.between(endTime, next.startTime).isNegative &&
            Duration.between(endTime, next.startTime).toMinutes() <= doubleLessonMaxBreakMinutes

    fun parseChange(lesson: Lesson): List<LessonChange>? {
        logger.debug("parsing lesson ({}) at ({})", lesson.subjects[0].longName, lesson.startTime)
        val name = lesson.subjects[0].longName
        val time = config[lesson.startTime.toKotlinLocalTime()] ?: run {
            logger.warn("invalid lesson time (${lesson.startTime})")
            return null
        }

        fun change(type: LessonChangeType, change: String?) =
            LessonChange(type, lesson.date, time..time, name, change, lesson.startTime, lesson.endTime)

        if (lesson.code == LessonCode.CANCELLED) return listOf(
            change(LessonChangeType.CANCELLED, null)
        ).also { logger.debug("found cancelled lesson ($time, $name)") }

        val changes = mutableListOf<LessonChange>()

        (lesson.originalTeachers.isEmpty()).ifFalse {
            changes += change(LessonChangeType.TEACHER, lesson.teachers.firstOrNull()?.longName ?: "---")
            logger.debug("found changed teacher ($time, $name)")
        }

        (lesson.originalRooms.isEmpty()).ifFalse {
            changes += change(LessonChangeType.ROOM, lesson.rooms.firstOrNull()?.name ?: "---")
            logger.debug("found changed room ($time, $name)")
        }

        return changes.takeIf { it.isNotEmpty() }
    }
}
