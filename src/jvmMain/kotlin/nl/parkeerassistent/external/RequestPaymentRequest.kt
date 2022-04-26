package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class RequestPaymentRequest(
    val amount: String
)