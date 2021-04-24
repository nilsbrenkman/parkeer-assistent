package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class ParkingSession(
    val Id: Int,
    val PermitId: Int,
    val TimeStartUtc: String,
    val TimeEndUtc: String,
    val CostMoney: Double,
    val LP: String
) {}
