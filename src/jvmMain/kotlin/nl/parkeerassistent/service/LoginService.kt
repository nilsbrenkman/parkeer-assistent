package nl.parkeerassistent.service

import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.Parameters
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.Log
import nl.parkeerassistent.Permit
import nl.parkeerassistent.Session
import nl.parkeerassistent.User
import nl.parkeerassistent.external.Credentials
import nl.parkeerassistent.external.Csrf
import nl.parkeerassistent.model.LoginRequest
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.monitoring.Monitoring

object LoginService {

    enum class Method : Monitoring.Method {
        LoggedIn,
        Login,
        Logout,
        ;
        override fun service(): Monitoring.Service {
            return Monitoring.Service.Login
        }
        override fun method(): String {
            return name
        }
    }

    suspend fun isLoggedIn(session: Session): Response {

        val result = session.client.get<nl.parkeerassistent.external.Session>(ApiHelper.getMainUrl("api/auth/session")) {}

        result.user?.let {
            Log.debug("token: ${it.access_token}")
            session.user = User(it.access_token)
            Log.debug("reportcode: ${it.reportcode}")
            if (session.permit == null) {
                session.permit = Permit(it.reportcode, null)
            }
            Monitoring.info(session.call, Method.LoggedIn, "LOGGED_IN")
            return Response(true, "Ingelogd")
        }

        Monitoring.info(session.call, Method.LoggedIn, "NOT_LOGGED_IN")
        return Response(false, "Niet ingelogd")

    }

    suspend fun login(session: Session, request: LoginRequest): Response {

        session.client.get<nl.parkeerassistent.external.Session>(ApiHelper.getMainUrl("api/auth/session")) {}

        val csrfToken = getCsrfToken(session)

        val result = session.client.post<Credentials>(ApiHelper.getMainUrl("api/auth/callback/credentials")) {
            body = FormDataContent(formData = Parameters.build {
                append("reportCode", request.username)
                append("pin", request.password)
                append("csrfToken", csrfToken)
                append("json", "true")
                append("callbackUrl", "https://aanmeldenparkeren.amsterdam.nl/login")
            })
        }

        Log.debug("credentials: ${result}")

        val loggedIn = isLoggedIn(session)

        Monitoring.info(session.call, Method.Login, if (loggedIn.success) "LOGIN_SUCCESS" else "LOGIN_FAILED")
        return loggedIn
    }

    suspend fun logout(session: Session): Response {
        val csrfToken = getCsrfToken(session)

        val result = session.client.post<Credentials>(ApiHelper.getMainUrl("api/auth/signout")) {
            body = FormDataContent(formData = Parameters.build {
                append("csrfToken", csrfToken)
                append("json", "true")
                append("callbackUrl", "https://aanmeldenparkeren.amsterdam.nl/login")
            })
        }

        if (result.url != "https://aanmeldenparkeren.amsterdam.nl/") {
            Monitoring.info(session.call, Method.Login, "LOGOUT_FAILED")
        }

        session.cookieStore.clear()
        session.user = null
        session.permit = null

        return Response(true)
    }

    suspend fun getCsrfToken(session: Session): String {
        val result = session.client.get<Csrf>(ApiHelper.getMainUrl("api/auth/csrf")) {}
        return result.csrfToken
    }


}