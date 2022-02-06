package nl.parkeerassistent.android.service.model

import kotlinx.serialization.Serializable

@Serializable
data class BalanceResponse(
    val balance: String
)
