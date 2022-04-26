package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val TransactionId: String,
    val Status: String
)
