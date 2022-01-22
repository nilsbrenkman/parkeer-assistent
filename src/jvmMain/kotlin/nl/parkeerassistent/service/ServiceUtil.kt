package nl.parkeerassistent.service

import io.ktor.application.ApplicationCall
import io.ktor.client.features.RedirectResponseException
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import nl.parkeerassistent.monitoring.Monitoring
import nl.parkeerassistent.external.BooleanResponse
import nl.parkeerassistent.model.Response
import org.apache.log4j.Logger
import java.util.UUID
import java.util.concurrent.TimeUnit

object ServiceUtil {

    val log = Logger.getLogger("ServiceUtil")

    suspend fun <RESPONSE> execute(
        method: Monitoring.Method,
        call: ApplicationCall,
        errorResponse: RESPONSE,
        function: suspend (call: ApplicationCall) -> RESPONSE
    ): RESPONSE {
        try {
            ensureWebUserId(call)
            return function.invoke(call)
        } catch (e: RedirectResponseException) {
            Monitoring.warn(call, method, "NOT_LOGGED_IN")
            call.response.status(HttpStatusCode.Forbidden)
        } catch (e: Exception) {
            log.warn("Unexpected error", e)
            Monitoring.warn(call, method, "ERROR")
            call.response.status(HttpStatusCode.ServiceUnavailable)
        }
        return errorResponse
    }

    suspend fun <REQUEST, RESPONSE> execute(
        method: Monitoring.Method,
        call: ApplicationCall,
        request: REQUEST,
        errorResponse: RESPONSE,
        function: suspend (call: ApplicationCall, request: REQUEST) -> RESPONSE
    ): RESPONSE {
        try {
            ensureWebUserId(call)
            return function.invoke(call, request)
        } catch (e: RedirectResponseException) {
            Monitoring.warn(call, method, "NOT_LOGGED_IN")
            call.response.status(HttpStatusCode.Forbidden)
        } catch (e: ServiceException) {
            log.warn("Service error [" + e.type + "]", e)
            Monitoring.warn(call, method, "SERVICE_ERROR")
            call.response.status(HttpStatusCode.ServiceUnavailable)
        } catch (e: Exception) {
            log.warn("Unexpected error", e)
            Monitoring.error(call, method, "INTERNAL_ERROR")
            call.response.status(HttpStatusCode.InternalServerError)
        }
        return errorResponse
    }

    fun convertResponse(call: ApplicationCall, method: Monitoring.Method, response: BooleanResponse): Response {
        if (response.successful) {
            Monitoring.info(call, method, "SUCCESS")
        } else {
            Monitoring.warn(call, method, "FAILED")
        }
        return Response(response.successful)
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