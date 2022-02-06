package nl.parkeerassistent.android.service.model

import kotlinx.serialization.Serializable
import nl.parkeerassistent.android.data.Visitor

@Serializable
data class StartParkingRequest(
    val visitor: Visitor,
    val timeMinutes: Int,
    val start: String?,
    val regimeTimeEnd: String
)
