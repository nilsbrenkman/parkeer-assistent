package nl.parkeerassistent.monitoring

import kotlinx.serialization.Serializable

@Serializable
data class QueryRequest(
    val query: Query,
)

@Serializable
data class Query(
    val range: Range,
)

@Serializable
data class Range(
    val date: DateRange,
)

@Serializable
data class DateRange(
    val lte: String,
)

@Serializable
data class QueryResponse(
    val total: Int,
    val deleted: Int,
)
