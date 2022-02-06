package nl.parkeerassistent.android.data

import kotlinx.serialization.Serializable

@Serializable
data class ParkingResponse(
    val active: List<Parking>,
    val scheduled: List<Parking>
)