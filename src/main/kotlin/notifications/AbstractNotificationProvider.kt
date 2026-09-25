package notifications

import config.NotificationConfig


abstract class AbstractNotificationProvider<T: NotificationConfig>(private val config: T) {
    abstract suspend fun sendMessage(message: String)
}
