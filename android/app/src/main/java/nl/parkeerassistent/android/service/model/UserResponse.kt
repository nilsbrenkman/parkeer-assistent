package nl.parkeerassistent.android.service.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val balance: String,
    val hourRate: Double,
    val regimeTimeStart: String,
    val regimeTimeEnd: String
)
