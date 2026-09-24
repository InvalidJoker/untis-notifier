package config
import kotlinx.serialization.Serializable

enum class ReminderDate{
    SAME_DAY,
    FULL_WEEK,
    DAY_BEFORE,
}

@Serializable
data class ReminderConfig(
    val newDateTime: kotlinx.datetime.LocalTime,
    val dates: List<ReminderDate> = listOf(ReminderDate.SAME_DAY)
)