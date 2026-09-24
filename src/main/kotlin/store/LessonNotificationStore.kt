package store

import config.ReminderDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ng.bossi.store.FsStoreDriver
import ng.bossi.store.typedStore
import untis.LessonChange
import java.time.LocalDate
import kotlin.io.path.Path

@Serializable
data class NotifiedChange(val reminders: Set<ReminderDate> = emptySet())

object LessonNotificationStore {

    private const val NAMESPACE = "lesson."

    private val driver = FsStoreDriver(Path("./cache"))

    val store = typedStore<NotifiedChange>(
        driver = driver,
        namespace = NAMESPACE,
        format = Json
    )

    private fun key(change: LessonChange) = "${change.date}.${change.lessonTime}.${change.type}"

    fun add(change: LessonChange, reminders: Collection<ReminderDate>) {
        val notified = store[key(change)] ?: NotifiedChange()
        store[key(change)] = notified.copy(reminders = notified.reminders + reminders)
    }

    fun has(change: LessonChange, reminder: ReminderDate): Boolean =
        store[key(change)]?.reminders?.contains(reminder) == true

    // removes all entries of lessons before the given date
    fun prune(before: LocalDate) = driver.all()
        .filter { key -> runCatching { LocalDate.parse(key.removePrefix(NAMESPACE).substringBefore('.')) < before }.getOrDefault(false) }
        .forEach { driver.remove(it) }

    fun clear() = store.clear()
}
