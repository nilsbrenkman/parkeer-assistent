package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class GetLPs(val data: List<LicensePlate>) {}