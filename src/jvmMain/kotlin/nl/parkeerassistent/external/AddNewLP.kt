package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class AddNewLP(
    val customerId: Int,
    val permitId: Int,
    val formattedLP: String,
    val comment: String
) {
}