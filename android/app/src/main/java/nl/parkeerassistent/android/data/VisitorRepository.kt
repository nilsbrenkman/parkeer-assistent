package nl.parkeerassistent.android.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.parkeerassistent.android.service.VisitorService
import javax.inject.Inject

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class VisitorRepository @Inject constructor(
    val state: State,
    val visitorService: VisitorService
) {

    suspend fun getVisitors(): List<Visitor> {
        return state.visitors ?: withContext(Dispatchers.IO) {
            val response = visitorService.getVisitors()
            state.visitors = response.visitors
            response.visitors
        }
    }

}