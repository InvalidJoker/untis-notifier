package store

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import ng.bossi.store.FsStoreDriver
import ng.bossi.store.typedStore
import ng.bossi.store.untypedStore
import utils.ifTrue
import java.io.File
import java.time.LocalDate
import kotlin.io.path.Path

object LessonNotificationStore {

    val store = untypedStore(
        driver = FsStoreDriver(Path("./cache")),
        format = Json
    )

    fun add(lessonTime: Int, lessonDate: LocalDate = LocalDate.now()): Boolean {
        has(lessonTime, lessonDate).ifTrue { return false }
        store["$lessonDate.$lessonTime"] = true
        return true
    }

    fun has(lessonTime: Int, lessonDate: LocalDate = LocalDate.now()): Boolean = store["$lessonDate.$lessonTime"] as? Boolean == true

    fun clear() = store.clear()
}