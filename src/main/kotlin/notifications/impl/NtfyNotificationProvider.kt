package notifications.impl

import config.NtfyNotificationConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ktor
import notifications.AbstractNotificationProvider

class NtfyNotificationProvider(
    private val config: NtfyNotificationConfig
) : AbstractNotificationProvider<NtfyNotificationConfig>(config) {

    companion object {
        private const val UNTIS_ICON =
            "https://www.untis.at/fileadmin/user_upload/Icon-1024x1024.svg"
    }

    override suspend fun sendMessage(message: String) {
        ktor.post("${config.url}/${config.topic}") {
            contentType(ContentType.Text.Plain)

            header("Title", "WebUntis Notification")
            header("Icon", UNTIS_ICON)
            header("Priority", "default")
            header("Tags", "school")

            setBody(message)

            if (config.username == null) {
                bearerAuth(config.password)
            } else {
                basicAuth(config.username, config.password)
            }
        }.let { response ->
            logger.info(
                "(NtfyRequest): status=${response.status}, " +
                        "message=${response.bodyAsText()}"
            )
        }
    }
}