package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class AddParking(
    val parkingsession: AddParkingSession
)

@Serializable
data class AddParkingSession(
    val reportCode: Long,
    val paymentZoneId: String,
    val vehicleId: String,
    val startDateTime: String,
    val endDateTime: String
)

@Serializable
data class ParkingOrder(
    val frontendId: Long,
    val orderStatus: String,
    val orderType: String
)

@Serializable
data class Order(
    val orderId: Long,
    val orderStatus: String,
    val orderType: String
)