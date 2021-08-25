package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class CalculateBalanceResponse(
    val regimeStartTime: String,
    val regimeEndTime: String
)