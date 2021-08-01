package nl.parkeerassistent.model

import kotlinx.serialization.Serializable

@Serializable
data class RegimeResponse (
    val regimeStartTime: String,
    val regimeEndTime: String
) {}