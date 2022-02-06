package nl.parkeerassistent.android.service

import androidx.core.text.isDigitsOnly
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.parkeerassistent.android.service.model.LoginRequest
import nl.parkeerassistent.android.service.model.Response
import javax.inject.Singleton

interface LoginService {

    suspend fun loggedIn(): Response
    suspend fun login(username: String, password: String): Response
    suspend fun logout(): Response

}

class LoginClient(
    private val client: ApiClient
) : LoginService {

    override suspend fun loggedIn(): Response {
        return client.get("login")
    }

    override suspend fun login(username: String, password: String): Response {
        return client.post("login", LoginRequest(username, password))
    }

    override suspend fun logout(): Response {
        return client.get("logout")
    }

}

class LoginMock : LoginService {

    private var loggedIn = false

    override suspend fun loggedIn(): Response {
        ServiceUtil.mockDelay()

        return Response(loggedIn)
    }

    override suspend fun login(username: String, password: String): Response {
        ServiceUtil.mockDelay()

        if (username.length < 4 || password.length != 4 || ! password.isDigitsOnly()) {
            return Response(false)
        }
        loggedIn = true
        return Response(true)
    }

    override suspend fun logout(): Response {
        ServiceUtil.mockDelay()

        loggedIn = false
        return Response(true)
    }

}

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {
    @Singleton
    @Provides
    fun provide(client: ApiClient) : LoginService {
        if (client.mock) {
            return LoginMock()
        }
        return LoginClient(client)
    }
}
