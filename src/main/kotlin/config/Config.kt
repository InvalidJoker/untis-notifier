package config

import json
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalTime
import kotlin.io.path.*

@Serializable
data class Config(
    val debug: Boolean = false,
    val untis: UntisConfig,
    val timetable: TimeTableConfig = mapOf(
        LocalTime.parse("07:50:00") to 1,
        // 5 min break
        LocalTime.parse("08:40:00") to 2,
        // Break 9:25-9:40
        LocalTime.parse("09:40:00") to 3,
        LocalTime.parse("10:25:00") to 4,
        // Break 11:10-11:30
        LocalTime.parse("11:30:00") to 5,
        LocalTime.parse("12:15:00") to 6,
        // Break 13:00-13:55
        LocalTime.parse("13:55:00") to 7,
        // 5 min break
        LocalTime.parse("14:45:00") to 8,
        // 5 min break
        LocalTime.parse("15:35:00") to 9,
        LocalTime.parse("16:20:00") to 10,
    ),
    val doubleLessonMaxBreakMinutes: Int = 0,
    val reminder: ReminderConfig = ReminderConfig(
        newDateTime = LocalTime.parse("07:30:00"),
        dates = listOf(ReminderDate.SAME_DAY, ReminderDate.FULL_WEEK)
    ),
    val notifications: NotificationConfig
)

fun loadConfig(): Config? =
    Path("config.json").let {
        if (it.exists()) {
            try {
                json.decodeFromString(it.readText())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            println("config.json does not exist, creating default config")
            val defaultConfig = Config(
                debug = false,
                untis = UntisConfig(
                    server = "https://example.com",
                    school = "school",
                    username = "username",
                    password = "password"
                ),
                notifications = NtfyNotificationConfig(
                    url = "https://ntfy.sh",
                    topic = "webuntis",
                    username = null,
                    password = "password"
                )
            )
            it.writeText(json.encodeToString(defaultConfig))
            defaultConfig
        }
    }
