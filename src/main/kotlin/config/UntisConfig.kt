package config

import kotlinx.serialization.Serializable

@Serializable
data class UntisConfig(
    val server: String,
    val school: String?,
    val username: String,
    val password: String,
    val refreshDelaySeconds: Int = 5 * 60
)