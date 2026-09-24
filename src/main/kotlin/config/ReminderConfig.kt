package config

import kotlinx.datetime.toJavaLocalTime
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

enum class ReminderDate(val daysBefore: Long) {
    SAME_DAY(0),
    DAY_BEFORE(1),
    // notifies as soon as a change within the next week is detected
    FULL_WEEK(7),
}

@Serializable
data class ReminderConfig(
    val newDateTime: kotlinx.datetime.LocalTime,
    val dates: List<ReminderDate> = listOf(ReminderDate.SAME_DAY)
) {
    val lookAheadDays: Long get() = dates.maxOfOrNull { it.daysBefore } ?: 0

    fun today(): LocalDate = LocalDateTime.now()
        .minus(Duration.ofSeconds(newDateTime.toJavaLocalTime().toSecondOfDay().toLong()))
        .toLocalDate()
}
