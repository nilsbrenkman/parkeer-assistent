package nl.parkeerassistent.service

import io.ktor.application.ApplicationCall
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.RedirectResponseException
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import nl.parkeerassistent.Session
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.monitoring.Monitoring
import org.apache.log4j.Logger
import java.util.UUID
import java.util.concurrent.TimeUnit

object ServiceUtil {

    val log = Logger.getLogger("ServiceUtil")

    suspend fun <RESPONSE> execute(
        method: Monitoring.Method,
        call: ApplicationCall,
        function: suspend (session: Session) -> RESPONSE
    ): RESPONSE? {
        return execute(method, call, object : Function<RESPONSE> {
            override suspend fun execute(session: Session): RESPONSE {
                return function.invoke(session)
            }
        })
    }

    suspend fun <REQUEST, RESPONSE> execute(
        method: Monitoring.Method,
        call: ApplicationCall,
        request: REQUEST,
        function: suspend (session: Session, request: REQUEST) -> RESPONSE
    ): RESPONSE? {
        return execute(method, call, object : FunctionWithRequest<REQUEST, RESPONSE>(request) {
            override suspend fun execute(session: Session): RESPONSE {
                return function.invoke(session, request)
            }
        })
    }

    private suspend fun <RESPONSE> execute(
        method: Monitoring.Method,
        call: ApplicationCall,
        function: Function<RESPONSE>
    ): RESPONSE? {
        try {
            val session = Session(call)
            ensureWebUserId(call)
            val response = function.execute(session)
            session.updateSessionCookie()
            return response
        } catch (e: RedirectResponseException) {
            Monitoring.warn(call, method, "NOT_LOGGED_IN")
            call.response.status(HttpStatusCode.Forbidden)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                Monitoring.warn(call, method, "NOT_LOGGED_IN")
                call.response.cookies.appendExpired("session")
                call.response.status(HttpStatusCode.Unauthorized)
            } else {
                log.warn("External error", e)
                Monitoring.warn(call, method, "EXTERNAL_ERROR")
                call.response.status(HttpStatusCode.ServiceUnavailable)
            }
        } catch (e: Exception) {
            log.warn("Unexpected error", e)
            Monitoring.warn(call, method, "ERROR")
            call.response.status(HttpStatusCode.ServiceUnavailable)
        }
        return null
    }

    fun convertResponse(call: ApplicationCall, method: Monitoring.Method, response: Boolean): Response {
        if (response) {
            Monitoring.info(call, method, "SUCCESS")
        } else {
            Monitoring.warn(call, method, "FAILED")
        }
        return Response(response)
    }

    fun ensureWebUserId(call: ApplicationCall) {
        if (call.request.headers["PA-OS"] == "Web") {
            val userId = call.request.cookies["userid"]
            if (userId == null) {
                val userIdCookie = Cookie("userid", UUID.randomUUID().toString(), maxAge = TimeUnit.DAYS.toSeconds(365).toInt())
                call.response.cookies.append(userIdCookie)
            }
        }
    }

}

private interface Function<RESPONSE> {
    suspend fun execute(session: Session): RESPONSE
}

private abstract class FunctionWithRequest <REQUEST, RESPONSE>(val request: REQUEST) :
    Function<RESPONSE>