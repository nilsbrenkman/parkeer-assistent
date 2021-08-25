package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class RequestPaymentResponse(
    val issuerAuthenticationUrl: String,
    val transactionId: String
)