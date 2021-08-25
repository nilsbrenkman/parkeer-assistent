package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class GetParkingSessions(val data: List<ParkingSession>)