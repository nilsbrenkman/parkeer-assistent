package nl.parkeerassistent.android.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.parkeerassistent.android.service.UserService
import javax.inject.Inject

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class UserRepository @Inject constructor(
    private val state: State,
    private val userService: UserService,
) {

    suspend fun getUser(): User {
        return state.user ?: withContext(Dispatchers.IO) {
            val response = userService.getUser()
            val user = User(response.balance, response.hourRate, Regime(response.regimeTimeStart, response.regimeTimeEnd))
            state.user = user
            user
        }
    }

    suspend fun getBalance(): String {
        return withContext(Dispatchers.IO) {
            val balance = userService.getBalance().balance
            state.user?.balance = balance
            balance
        }
    }

}