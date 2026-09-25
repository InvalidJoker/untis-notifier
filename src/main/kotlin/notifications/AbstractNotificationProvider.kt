package notifications

import config.NotificationConfig
import utils.getLogger


abstract class AbstractNotificationProvider<T: NotificationConfig>(private val config: T) {
    protected val logger = getLogger(this::class)
    abstract suspend fun sendMessage(message: String)
}
