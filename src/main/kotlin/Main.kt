import config.DiscordNotificationConfig
import config.NtfyNotificationConfig
import config.PushoverNotificationConfig
import config.loadConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import notifications.MessageFormatter
import notifications.impl.DiscordNotificationProvider
import notifications.impl.NtfyNotificationProvider
import notifications.impl.PushoverNotificationProvider
import store.LessonNotificationStore
import untis.LessonParser
import untis.closingUntisSession
import untis.timetable
import utils.getLogger
import kotlin.time.Duration.Companion.seconds

val ktor by lazy { HttpClient(CIO) }

val json = Json {
    classDiscriminator = "type"
    allowTrailingComma = true
    isLenient = true
}

val mainLogger = getLogger("Main")

suspend fun main() = coroutineScope {
    val config = loadConfig() ?: error("cannot read config")

    val notificationProvider = when (config.notifications) {
        is PushoverNotificationConfig -> {
            mainLogger.info("initializing Pushover notification provider")
            PushoverNotificationProvider(config.notifications)
        }

        is NtfyNotificationConfig -> {
            mainLogger.info("initializing Ntfy notification provider")
            NtfyNotificationProvider(config.notifications)
        }

        is DiscordNotificationConfig -> {
            mainLogger.info("initializing Discord notification provider")
            DiscordNotificationProvider(config.notifications)
        }
    }

    val lessonParser = LessonParser(config.timetable, config.doubleLessonMaxBreakMinutes)

    val messageFormatter = MessageFormatter(config.messages)

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
                        mainLogger.debug(
                            "change ({}, {}, {}) has already been noticed or is not due yet",
                            change.date,
                            change.lessonTimeLabel,
                            change.lessonName
                        )
                        continue
                    }
                    LessonNotificationStore.add(change, dueReminders)
                    notificationProvider.sendMessage(messageFormatter.format(change, today))
                }
            }
            delay(config.untis.refreshDelaySeconds.seconds)
        }
    }

    Unit
}