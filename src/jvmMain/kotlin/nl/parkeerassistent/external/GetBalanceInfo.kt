package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class GetBalanceInfo(
    val hourRate: Double,
    val regimeStartTime: String,
    val regimeEndTime: String
)