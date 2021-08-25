package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class StopParkingSession(
    val parkingSessionId: Int
)