package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class Permits(
    val permits: List<Permit>,
    val wallet: Wallet = Wallet(0.0, "EUR")
)

@Serializable
data class Permit(
    val reportCode: Long,
    val paymentZones: List<PaymentZone>,
    val parkingRate: Rate
)

@Serializable
data class PaymentZone(
    val id: String,
    val description: String,
    val days: List<Day>
)

@Serializable
data class Day(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)

@Serializable
data class Rate(
    val currency: String,
    val value: Double
)

@Serializable
data class Wallet(
    val balance: Double,
    val currency: String
)