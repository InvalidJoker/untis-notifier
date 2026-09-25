package untis

import java.time.LocalDate
import java.time.LocalTime

enum class LessonChangeType {
    CANCELLED,
    TEACHER,
    ROOM
}

data class LessonChange(
    val type: LessonChangeType,
    val date: LocalDate,
    val lessonTimes: IntRange,
    val lessonName: String,
    val change: String?,
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    val lessonTimeLabel: String
        get() = if (lessonTimes.first == lessonTimes.last) "${lessonTimes.first}" else "${lessonTimes.first}-${lessonTimes.last}"
}
