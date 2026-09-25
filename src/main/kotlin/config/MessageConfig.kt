package config

import kotlinx.serialization.Serializable

@Serializable
data class MessageConfig(
    // used for weekday names
    val locale: String = "en",
    val cancelled: String = "Lesson {lesson} ({subject}) {day} has been cancelled",
    val room: String = "Lesson {lesson} ({subject}) {day} has been assigned a new room: {change}",
    val teacher: String = "Lesson {lesson} ({subject}) {day} has been assigned a new teacher: {change}",
    val subject: String = "Lesson {lesson} ({subject}) {day} has been assigned a new subject: {change}",
    val additional: String = "Lesson {lesson} ({subject}) {day} has been added",
    val today: String = "today",
    val tomorrow: String = "tomorrow",
    val weeks: Map<Int, String> = emptyMap(),
    val otherDay: String = "on {weekday} ({date})",
)
