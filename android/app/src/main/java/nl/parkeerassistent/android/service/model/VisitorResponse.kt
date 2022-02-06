package nl.parkeerassistent.android.service.model

import kotlinx.serialization.Serializable
import nl.parkeerassistent.android.data.Visitor

@Serializable
data class VisitorResponse(
    val visitors: List<Visitor>
)