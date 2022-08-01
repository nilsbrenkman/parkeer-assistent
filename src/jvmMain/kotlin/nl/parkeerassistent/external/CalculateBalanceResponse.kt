package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class CalculateBalanceResponse(
    val hourRate: Double,
    val regimeStartTime: String,
    val regimeEndTime: String
)