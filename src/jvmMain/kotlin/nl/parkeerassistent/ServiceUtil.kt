package nl.parkeerassistent

import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.http.*
import nl.parkeerassistent.external.BooleanResponse
import nl.parkeerassistent.model.Response
import org.apache.log4j.Logger

object ServiceUtil {

    val log = Logger.getLogger("ServiceUtil")

    suspend fun <RESPONSE> execute(
        method: Monitoring.Method,
        call: ApplicationCall,
        errorResponse: RESPONSE,
        function: suspend (call: ApplicationCall) -> RESPONSE
    ): RESPONSE {
        try {
            return function.invoke(call)
        } catch (e: RedirectResponseException) {
            Monitoring.warn(method, "NOT_LOGGED_IN")
            call.response.status(HttpStatusCode.Forbidden)
        } catch (e: Exception) {
            log.warn("Unexpected error", e)
            Monitoring.warn(method, "ERROR")
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
            return function.invoke(call, request)
        } catch (e: RedirectResponseException) {
            Monitoring.warn(method, "NOT_LOGGED_IN")
            call.response.status(HttpStatusCode.Forbidden)
        } catch (e: Exception) {
            log.warn("Unexpected error", e)
            Monitoring.warn(method, "ERROR")
            call.response.status(HttpStatusCode.ServiceUnavailable)
        }
        return errorResponse
    }

    fun convertResponse(method: Monitoring.Method, response: BooleanResponse): Response {
        if (response.successful) {
            Monitoring.info(method, "SUCCESS")
        } else {
            Monitoring.info(method, "FAILED")
        }
        return Response(response.successful)
    }

}