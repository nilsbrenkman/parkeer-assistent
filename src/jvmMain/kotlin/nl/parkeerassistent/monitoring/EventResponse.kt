package nl.parkeerassistent.monitoring

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventResponse(
    @SerialName("_index") val index: String,
    @SerialName("_type") val type: String,
    @SerialName("_id") val id: String,
    @SerialName("_version") val version: Int,
    @SerialName("_shards") val shards: Shards,
    val result: String,
) {
    @Serializable
    data class Shards(
        val total: Int,
        val successful: Int,
        val failed: Int,
    ) { }
}
