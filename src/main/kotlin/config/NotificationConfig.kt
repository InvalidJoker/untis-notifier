package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface NotificationConfig

@Serializable
@SerialName("Pushover")
data class PushoverNotificationConfig(
    val apiKey: String,
    val groupKey: String,
) : NotificationConfig

@Serializable
@SerialName("Ntfy")
data class NtfyNotificationConfig(
    val url: String,
    val topic: String,
    val username: String?,
    val password: String,
) : NotificationConfig

@Serializable
@SerialName("Discord")
data class DiscordNotificationConfig(
    val webhookUrl: String,
) : NotificationConfig

