package untis

import java.time.LocalDate

enum class LessonChangeType {
    CANCELLED,
    TEACHER,
    ROOM
}

data class LessonChange(
    val type: LessonChangeType,
    val date: LocalDate,
    val lessonTime: Int,
    val lessonName: String,
    val change: String?
)