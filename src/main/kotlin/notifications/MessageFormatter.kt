package notifications

import config.MessageConfig
import untis.LessonChange
import untis.LessonChangeType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

class MessageFormatter(private val config: MessageConfig) {

    private val locale = Locale.forLanguageTag(config.locale)
    private val placeholder = Regex("""\{(\w+)}""")

    private fun String.fill(values: Map<String, Any?>) =
        placeholder.replace(this) { match -> values[match.groupValues[1]]?.toString() ?: match.value }

    private fun day(date: LocalDate, today: LocalDate): String {
        if (date == today) return config.today
        if (date == today.plusDays(1)) return config.tomorrow
        val weeks = ChronoUnit.WEEKS.between(today.with(DayOfWeek.MONDAY), date.with(DayOfWeek.MONDAY)).toInt()
        return (config.weeks[weeks] ?: config.otherDay).fill(
            mapOf(
                "weekday" to date.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
                "date" to date,
                "weeks" to weeks,
            )
        )
    }

    fun format(change: LessonChange, today: LocalDate): String = when (change.type) {
        LessonChangeType.CANCELLED -> config.cancelled
        LessonChangeType.ROOM -> config.room
        LessonChangeType.TEACHER -> config.teacher
    }.fill(
        mapOf(
            "lesson" to change.lessonTimeLabel,
            "subject" to change.lessonName,
            "day" to day(change.date, today),
            "change" to change.change,
        )
    )
}
