package nl.parkeerassistent.android.data
import kotlinx.serialization.Serializable

@Serializable
data class Issuer(
    val issuerId: String,
    val name: String
) {
    override fun toString(): String = name
}
