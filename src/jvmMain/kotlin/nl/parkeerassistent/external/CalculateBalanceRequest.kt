package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class CalculateBalanceRequest(
    val permitId: Int,
    val timeStartUtc: String,
    val timeEndUtc: String
) {}