import config.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import notifications.impl.NtfyNotificationProvider
import notifications.impl.PushoverNotificationProvider
import notifications.impl.DiscordNotificationProvider
import store.LessonNotificationStore
import untis.LessonParser
import untis.closingUntisSession
import untis.timetable
import utils.d
import utils.e
import utils.i
import kotlin.properties.Delegates
import kotlin.time.Duration.Companion.seconds

val ktor by lazy { HttpClient(CIO) }

var debug by Delegates.notNull<Boolean>()
    private set

val json = Json {
    classDiscriminator = "type"
    allowTrailingComma = true
    isLenient = true
}

suspend fun main() = coroutineScope {
    val config = loadConfig() ?: e("cannot read config")
    debug = config.debug
    val notificationProvider = when (config.notifications) {
        is PushoverNotificationConfig -> {
            i("initializing Pushover notification provider")
            PushoverNotificationProvider(config.notifications)
        }
        is NtfyNotificationConfig -> {
            i("initializing Ntfy notification provider")
            NtfyNotificationProvider(config.notifications)
        }
        is DiscordNotificationConfig -> {
            i("initializing Discord notification provider")
            DiscordNotificationProvider(config.notifications)
        }
    }

    val lessonParser = LessonParser(config.timetable, config.doubleLessonMaxBreakMinutes)

    launch {
        while (isActive) {
            val today = config.reminder.today()
            LessonNotificationStore.prune(before = today)
            closingUntisSession(config.untis) { session ->
                val timeTable = session.timetable(today, config.reminder.lastLessonDate(today))
                for (change in lessonParser.parseChanges(timeTable)) {
                    val dueReminders = config.reminder.dates
                        .filter { !it.firstDay(change.date).isAfter(today) }
                        .filterNot { LessonNotificationStore.has(change, it) }
                    if (dueReminders.isEmpty()) {
                        d("change (${change.date}, ${change.lessonTimeLabel}, ${change.lessonName}) has already been noticed or is not due yet")
                        continue
                    }
                    LessonNotificationStore.add(change, dueReminders)
                    notificationProvider.sendChanges(today, change)
                }
            }
            delay(config.untis.refreshDelaySeconds.seconds)
        }
    }

    Unit
}