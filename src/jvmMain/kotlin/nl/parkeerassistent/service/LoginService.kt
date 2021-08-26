package nl.parkeerassistent.service

import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.Monitoring
import nl.parkeerassistent.Session
import nl.parkeerassistent.model.LoginRequest
import nl.parkeerassistent.model.Response
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.FormElement

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

    suspend fun isLoggedIn(call: ApplicationCall): Response {
        val sessionCookie = call.request.cookies["session"]
        if (sessionCookie == null) {
            Monitoring.info(Method.LoggedIn, "NO_SESSION")
            return Response(false, "Geen sessie cookie")
        }
        val session = Session(sessionCookie)

        try {
            ApiHelper.client.get<String>(ApiHelper.getUrl("General/GetExpirationTimeInSeconds")) {
                ApiHelper.addHeaders(this, session)
            }
            Monitoring.info(Method.LoggedIn, "LOGGED_IN")
            return Response(true, "Ingelogd")
        } catch (e: RedirectResponseException) {
            Monitoring.info(Method.LoggedIn, "NOT_LOGGED_IN")
            return Response(false, "Niet ingelogd")
        }
    }

    fun login(call: ApplicationCall, request: LoginRequest): Response {

        val session = Session(call.request.cookies["session"])

        val login = session.send(Jsoup.connect(ApiHelper.baseUrl).method(Connection.Method.GET))

        if ("Inloggen - Parkeerapplicatie" != login.title()) {
            Monitoring.warn(Method.Login, "UNABLE_TO_LOAD_PAGE")
            return Response(false, "Kan login pagina niet laden")
        }

        val connection = login.select(".account-wall").forms()[0].submit()
        connection.data("Email").value(request.username)
        connection.data("Password").value(request.password)
        val home = session.send(connection)

        if ("Klant startpagina - Parkeerapplicatie" != home.title()) {
            Monitoring.info(Method.Login, "LOGIN_FAILED")
            return Response(false, "Inloggen mislukt")
        }

        val sessionCookie = Cookie("session", session.header())
        call.response.cookies.append(sessionCookie)

        val customerId = findCustomerId(home.body().html())
        if (customerId == null) {
            Monitoring.warn(Method.Login, "CUSTOMER_ID_NOT_FOUND")
            return Response(false, "Kan klantnummer niet vinden")
        }
        val customerIdCookie = Cookie("customerid", customerId)
        call.response.cookies.append(customerIdCookie)

        Monitoring.info(Method.Login, "LOGIN_SUCCESS")
        return Response(true)
    }

    private val regex = "\\\\\"customerId\\\\\":([0-9]{6})".toRegex()

    fun findCustomerId(html: String): String? {
        val match = regex.find(html)
        return match?.groupValues?.get(1)
    }

    fun logout(call: ApplicationCall): Response {
        val session = Session(call.request.cookies["session"])

        val logout = session.send(Jsoup.connect(ApiHelper.baseUrl).method(Connection.Method.GET))

        if ("Klant startpagina - Parkeerapplicatie" != logout.title()) {
            Monitoring.info(Method.Logout, "NOT_LOGGED_IN")
            return Response(false, "Niet ingelogd")
        }

        val connection = (logout.select("#logoutForm")[0] as FormElement).submit()
        val login = session.send(connection)

        if ("Inloggen - Parkeerapplicatie" != login.title()) {
            Monitoring.warn(Method.Logout, "LOGOUT_FAILED")
            return Response(false, "Uitloggen mislukt")
        }

        call.response.cookies.append(Cookie("session", session.header()))

        Monitoring.info(Method.Logout, "LOGOUT_SUCCESS")
        return Response(true)
    }

}