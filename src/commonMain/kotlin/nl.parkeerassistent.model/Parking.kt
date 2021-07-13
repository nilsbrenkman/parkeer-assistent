package nl.parkeerassistent.model

import kotlinx.serialization.Serializable

@Serializable
data class Parking(
    val id: Int,
    val license: String,
    val startTime: String,
    val endTime: String,
    val cost: Double
) {}