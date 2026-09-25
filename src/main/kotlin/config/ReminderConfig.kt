package config

import kotlinx.datetime.toJavaLocalTime
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

enum class ReminderDate {
    SAME_DAY,
    DAY_BEFORE,
    FULL_WEEK,
    SEVEN_DAYS,
    FULL_WEEK_AND_SEVEN_DAYS,
    FOURTEEN_DAYS;

    fun firstDay(lessonDate: LocalDate): LocalDate = when (this) {
        SAME_DAY -> lessonDate
        DAY_BEFORE -> lessonDate.minusDays(1)
        FULL_WEEK -> lessonDate.with(DayOfWeek.MONDAY)
        SEVEN_DAYS -> lessonDate.minusDays(7)
        FULL_WEEK_AND_SEVEN_DAYS -> lessonDate.minusDays(7).with(DayOfWeek.MONDAY)
        FOURTEEN_DAYS -> lessonDate.minusDays(14)
    }

    fun lastLessonDate(today: LocalDate): LocalDate = when (this) {
        SAME_DAY -> today
        DAY_BEFORE -> today.plusDays(1)
        FULL_WEEK -> today.with(DayOfWeek.SUNDAY)
        SEVEN_DAYS -> today.plusDays(7)
        FULL_WEEK_AND_SEVEN_DAYS -> today.plusDays(7).with(DayOfWeek.SUNDAY)
        FOURTEEN_DAYS -> today.plusDays(14)
    }
}

@Serializable
data class ReminderConfig(
    val newDateTime: kotlinx.datetime.LocalTime,
    val dates: List<ReminderDate>
) {
    fun lastLessonDate(today: LocalDate): LocalDate = dates.maxOfOrNull { it.lastLessonDate(today) } ?: today

    fun today(): LocalDate = LocalDate.now()
        .minus(Duration.ofSeconds(newDateTime.toJavaLocalTime().toSecondOfDay().toLong()))
}
