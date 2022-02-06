package nl.parkeerassistent.android.service.model
import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val status: String
)
