package nl.parkeerassistent.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val balance: String,
    val hourRate: Double,
    val regimeTimeStart: String,
    val regimeTimeEnd: String,
    val regime: Regime
)

@Serializable
data class Regime(
    val days: List<RegimeDay>
)

@Serializable
data class RegimeDay(
    val weekday: String,
    val startTime: String,
    val endTime: String
)