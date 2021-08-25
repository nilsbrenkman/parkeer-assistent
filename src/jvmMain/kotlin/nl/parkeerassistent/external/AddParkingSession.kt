package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class AddParkingSession(
    val permitId: Int,
    val lp: String,
    val timeStartUtc: String,
    val timeEndUtc: String,
    val regimeEndTime: String,
    val endTimeParkingRegimeEnd: Boolean,
    val saveMode: Boolean
)