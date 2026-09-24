package notifications

import config.NotificationConfig
import untis.LessonChange
import untis.LessonChangeType
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


abstract class AbstractNotificationProvider<T: NotificationConfig>(private val config: T) {
    protected abstract suspend fun sendMessage(message: String)

    private fun dayLabel(date: LocalDate, today: LocalDate) = when (date) {
        today -> "today"
        today.plusDays(1) -> "tomorrow"
        else -> "on ${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ($date)"
    }

    suspend fun sendChanges(today: LocalDate, vararg changes: LessonChange) = changes.forEach {
        val day = dayLabel(it.date, today)
        when (it.type) {
            LessonChangeType.CANCELLED -> {
                sendMessage("Lesson ${it.lessonTime} ${it.lessonName} $day has been cancelled")
            }
            LessonChangeType.ROOM -> {
                sendMessage("Lesson ${it.lessonTime} (${it.lessonName}) $day has been assigned a new room: ${it.change}")
            }
            LessonChangeType.TEACHER -> {
                sendMessage( "Lesson ${it.lessonTime} (${it.lessonName}) $day has been assigned a new teacher: ${it.change}")
            }
        }
    }
}
