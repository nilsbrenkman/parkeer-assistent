package nl.parkeerassistent.monitoring

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val date: String,
    val userId: String,
    val os: String,
    val version: String,
    val build: Int,
    val service: String,
    val method: String,
    val level: String,
    val message: String,
)