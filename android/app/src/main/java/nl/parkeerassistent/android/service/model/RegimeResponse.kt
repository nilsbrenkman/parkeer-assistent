package nl.parkeerassistent.android.service.model

import kotlinx.serialization.Serializable

@Serializable
data class RegimeResponse(
    val regimeTimeStart: String,
    val regimeTimeEnd: String
)
