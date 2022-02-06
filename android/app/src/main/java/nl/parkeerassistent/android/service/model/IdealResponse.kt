package nl.parkeerassistent.android.service.model
import kotlinx.serialization.Serializable
import nl.parkeerassistent.android.data.Issuer

@Serializable
data class IdealResponse(
    val amounts: List<String>,
    val issuers: List<Issuer>
)
