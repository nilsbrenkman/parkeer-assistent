package nl.parkeerassistent

import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.request.*
import kotlinx.coroutines.runBlocking
import nl.parkeerassistent.model.LoginRequest
import nl.parkeerassistent.model.Response
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.FormElement

object LoginService {

    fun isLoggedIn(request: ApplicationRequest): Response {
        val sessionCookie = request.cookies["session"]
        if (sessionCookie == null) {
            return Response(false, "Geen sessie cookie")
        }
        val session = Session(sessionCookie)
        try {
            runBlocking {
                ApiHelper.client.get<String>(ApiHelper.getUrl("General/GetExpirationTimeInSeconds")) {
                    ApiHelper.addHeaders(this, session)
                }
            }
            return Response(true, "Ingelogd")
        } catch (e: RedirectResponseException) {
            //
        }
        return Response(false, "Niet ingelogd")
    }

    fun login(request: LoginRequest, call: ApplicationCall): Response {

        val session = Session(call.request.cookies["session"])

        val login = session.send(Jsoup.connect(ApiHelper.baseUrl).method(Connection.Method.GET))

        if ("Inloggen - Parkeerapplicatie" != login.title()) {
            return Response(false, "Kan login pagina niet laden")
        }

        val connection = login.select(".account-wall").forms()[0].submit()
        connection.data("Email").value(request.username)
        connection.data("Password").value(request.password)
        val home = session.send(connection)

        if ("Klant startpagina - Parkeerapplicatie" != home.title()) {
            return Response(false, "Inloggen mislukt")
        }

        val sessionCookie = Cookie("session", session.header())
        call.response.cookies.append(sessionCookie)

        val customerId = findCustomerId(home.body().html())
        if (customerId == null) {
            return Response(false, "Kan klantnummer niet vinden")
        }
        val customerIdCookie = Cookie("customerid", customerId)
        call.response.cookies.append(customerIdCookie)

        return Response(true)
    }

    private val regex = "\\\\\"customerId\\\\\":([0-9]{6})".toRegex()

    fun findCustomerId(html: String): String? {
        val match = regex.find(html)
        return match?.groupValues?.get(1)
    }

    fun logout(call: ApplicationCall) : Response {
        val session = Session(call.request.cookies["session"])

        val logout = session.send(Jsoup.connect(ApiHelper.baseUrl).method(Connection.Method.GET))

        if ("Klant startpagina - Parkeerapplicatie" != logout.title()) {
            return Response(false, "Niet ingelogd")
        }

        val connection = (logout.select("#logoutForm")[0] as FormElement).submit()
        val login = session.send(connection)

        if ("Inloggen - Parkeerapplicatie" != login.title()) {
            return Response(false, "Uitloggen mislukt")
        }

        call.response.cookies.append(Cookie("session", session.header()))
        return Response(true)
    }

}