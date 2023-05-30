package nl.parkeerassistent.monitoring

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    @SerialName(value = "@timestamp")
    val timestamp: String,
    val userId: String,
    val os: String,
    val sdk: String,
    val version: String,
    val build: Int,
    val service: String,
    val method: String,
    val level: String,
    val message: String,
)