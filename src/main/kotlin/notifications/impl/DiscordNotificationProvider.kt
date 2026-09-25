package notifications.impl

import config.DiscordNotificationConfig
import io.ktor.client.request.*
import io.ktor.http.*
import json
import kotlinx.serialization.Serializable
import ktor
import notifications.AbstractNotificationProvider

class DiscordNotificationProvider(val config: DiscordNotificationConfig) :
    AbstractNotificationProvider<DiscordNotificationConfig>(config) {

    @Serializable
    private data class WebhookMessage(val content: String)

    override suspend fun sendMessage(message: String) {
        ktor.post(config.webhookUrl) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(json.encodeToString(WebhookMessage(message)))
        }
    }
}