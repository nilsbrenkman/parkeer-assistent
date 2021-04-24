package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class GetPermitsByCustomer(val data: List<Permit>) {
}