package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val balance: Balance,
    val redirect: Redirect
)

@Serializable
data class Balance(
    val amount: Double,
    val currency: String
)

@Serializable
data class Redirect(
    val merchantReturnUrl: String
)

@Serializable
data class PaymentOrder(
    val frontendId: Long,
    val redirectUrl: String,
    val orderStatus: String,
    val orderType: String
)