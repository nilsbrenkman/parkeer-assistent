package nl.parkeerassistent.model

import kotlinx.serialization.Serializable

@Serializable
data class Visitor(
    val visitorId: Int,
    val permitId: Int,
    val license: String,
    val formattedLicense: String,
    val name: String?
) {

    fun full(): String {
        if (name != null) {
            return "$license / $name"
        }
        return license
    }

}