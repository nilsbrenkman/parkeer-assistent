package nl.parkeerassistent.model

import kotlinx.serialization.Serializable

@Serializable
data class History(
    val id: Int,
    val license: String,
    val name: String?,
    val startTime: String,
    val endTime: String,
    val cost: Double
) {}