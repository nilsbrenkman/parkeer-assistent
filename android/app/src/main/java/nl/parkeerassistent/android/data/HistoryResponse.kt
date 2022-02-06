package nl.parkeerassistent.android.data

import kotlinx.serialization.Serializable

@Serializable
data class HistoryResponse(
    val history: List<Parking>
)