package nl.parkeerassistent.android.service.model
import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val amount: String,
    val issuerId: String
)
