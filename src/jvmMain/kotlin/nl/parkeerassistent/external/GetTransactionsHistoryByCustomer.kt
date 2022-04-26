package nl.parkeerassistent.external

import kotlinx.serialization.Serializable

@Serializable
data class GetTransactionsHistoryByCustomer(val data: List<Transaction>)