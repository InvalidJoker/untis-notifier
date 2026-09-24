package untis

import config.TimeTableConfig
import kotlinx.datetime.toKotlinLocalTime
import utils.d
import org.bytedream.untis4j.UntisUtils.LessonCode
import org.bytedream.untis4j.responseObjects.Timetable.Lesson
import utils.ifFalse
import utils.w

class LessonParser(val config: TimeTableConfig) {

    fun parseChange(lesson: Lesson): List<LessonChange>? {
        d("parsing lesson (${lesson.subjects[0].longName}) at (${lesson.startTime})")
        val name = lesson.subjects[0].longName
        val time = config[lesson.startTime.toKotlinLocalTime()] ?: run {
            w("invalid lesson time (${lesson.startTime})")
            return null
        }

        if (lesson.code == LessonCode.CANCELLED) return listOf(
            LessonChange(
                LessonChangeType.CANCELLED,
                lesson.date,
                time,
                name,
                null
            )
        ).also { d("found cancelled lesson ($time, $name)") }

        val changes = mutableListOf<LessonChange>()

        (lesson.originalTeachers.isEmpty()).ifFalse {
            changes += LessonChange(LessonChangeType.TEACHER, lesson.date, time, name, lesson.teachers.firstOrNull()?.longName ?: "---")
            d("found changed teacher ($time, $name)")
        }

        (lesson.originalRooms.isEmpty()).ifFalse {
            changes += LessonChange(LessonChangeType.ROOM, lesson.date, time, name, lesson.rooms.firstOrNull()?.name ?: "---")
            d("found changed room ($time, $name)")
        }

        return changes.takeIf { it.isNotEmpty() }
    }
}
