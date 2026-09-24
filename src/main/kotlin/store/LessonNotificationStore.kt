package store

import config.ReminderDate
import kotlinx.serialization.json.Json
import ng.bossi.store.FsStoreDriver
import ng.bossi.store.untypedStore
import untis.LessonChange
import java.time.LocalDate
import kotlin.io.path.Path

object LessonNotificationStore {

    private val driver = FsStoreDriver(Path("./cache"))

    val store = untypedStore(
        driver = driver,
        format = Json
    )

    private fun key(change: LessonChange, reminder: ReminderDate) =
        "${change.date}.${change.lessonTime}.${change.type}.$reminder"

    fun add(change: LessonChange, reminder: ReminderDate) {
        store[key(change, reminder)] = true
    }

    fun has(change: LessonChange, reminder: ReminderDate): Boolean = store[key(change, reminder)] as? Boolean == true

    // removes all entries of lessons before the given date
    fun prune(before: LocalDate) = driver.all()
        .filter { key -> runCatching { LocalDate.parse(key.substringBefore('.')) < before }.getOrDefault(false) }
        .forEach { driver.remove(it) }

    fun clear() = store.clear()
}
