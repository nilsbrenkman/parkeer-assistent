package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class StopParking(
    val parkingsession: StopParkingSession
)

@Serializable
data class StopParkingSession(
    val reportCode: Long,
    val psRightId: Long,
    val startDateTime: String,
    val endDateTime: String
)
