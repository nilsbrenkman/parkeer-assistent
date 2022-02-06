package nl.parkeerassistent.android.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.parkeerassistent.android.service.LoginService
import nl.parkeerassistent.android.service.model.Response
import javax.inject.Inject

class LoginRepository @Inject constructor(
    val state: State,
    private val loginService: LoginService,
    private val userRepository: UserRepository
) {

    suspend fun isLoggedIn(): Response {
        return state.loggedIn ?: withContext(Dispatchers.IO) {
            Log.d("LoginRepository", "isLoggedIn")
            val response = loginService.loggedIn()
            state.loggedIn = response
            response
        }
    }

    suspend fun login(username: String, password: String): Response {
        return withContext(Dispatchers.IO) {
            val response: Response = loginService.login(username, password)
            state.loggedIn = response
            if (response.success) {
                userRepository.getUser()
            }
            response
        }
    }

    suspend fun logout(): Response {
        return withContext(Dispatchers.IO) {
            val response = loginService.logout()
            state.reset()
            response
        }
    }

}