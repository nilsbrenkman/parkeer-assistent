package nl.parkeerassistent.model

import kotlinx.serialization.Serializable

@Serializable
data class Parking(
    val id: Int,
    val license: String,
    val startTime: String,
    val endTime: String,
    val cost: Double
) {

    fun timeRange(): String {
        val singleDay = startTime.substring(0, 5) == endTime.substring(0, 5)
        if (singleDay) {
            return "$startTime - ${endTime.substring(6)}"
        }
        return "$startTime - $endTime"
    }

}