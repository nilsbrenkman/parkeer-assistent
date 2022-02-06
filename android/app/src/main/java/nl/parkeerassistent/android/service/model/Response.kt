package nl.parkeerassistent.android.service.model

import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val success: Boolean,
    val message: String? = null,
)
