package nl.parkeerassistent.model

import kotlinx.serialization.Serializable

@Serializable
data class Visitor(
    val visitorId: Int,
    val permitId: Long,
    val license: String,
    val formattedLicense: String,
    val name: String? = null
) {

    fun full(): String {
        if (name != null && name != "") {
            return "$license / $name"
        }
        return license
    }

}