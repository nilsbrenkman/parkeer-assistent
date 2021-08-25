package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class LicensePlate(
    val LPId: Int,
    val PermitId: Int,
    val LP: String,
    val FormattedLP: String,
    val Comment: String?
)