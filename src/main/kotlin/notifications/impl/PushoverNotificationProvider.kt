package notifications.impl

import config.PushoverNotificationConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import ktor
import notifications.AbstractNotificationProvider

class PushoverNotificationProvider(private val config: PushoverNotificationConfig) : AbstractNotificationProvider<PushoverNotificationConfig>(config) {
    override suspend fun sendMessage(message: String) {
        ktor.post("https://api.pushover.net/1/messages.json") {
            parameter("token", config.apiKey)
            parameter("user", config.groupKey)
            parameter("message", message)
        }.let { response ->
            logger.info("(PushoverRequest) status=${response.status}, message=${response.bodyAsText()}")
        }
    }
}