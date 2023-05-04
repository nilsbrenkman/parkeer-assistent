package nl.parkeerassistent

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.features.HttpResponseValidator
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import nl.parkeerassistent.external.Order
import nl.parkeerassistent.service.ServiceException

object ApiHelper {

    val mainBaseUrl = "https://aanmeldenparkeren.amsterdam.nl/"
    val cloudBaseUrl = "https://evs-ssp.mendixcloud.com/rest/sspapi/"

    val jsonConfig: Json = Json {
        isLenient = false
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = false
    }

    val client = HttpClient(Java) {
        followRedirects = false
        engine {
            threadsCount = 10
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(jsonConfig)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status.value / 100 != 2) {
                    throw ServiceException(ServiceException.Type.API, "Request failed [status=${response.status.value}]")
                }
            }
        }
    }

    fun getMainUrl(url: String):String {
        return mainBaseUrl + url
    }

    fun getCloudUrl(url: String):String {
        return cloudBaseUrl + url
    }

    fun addCloudHeaders(httpRequestBuilder: HttpRequestBuilder, session: CallSession) {
        val token = ensureData(session.user?.token, "token")
        httpRequestBuilder.header("Authorization", token)
        httpRequestBuilder.contentType(ContentType.Application.Json)
    }

    suspend fun waitForOrder(session: CallSession, orderId: Long): Boolean {
        repeat(5) {
            withContext(Dispatchers.IO) {
                Thread.sleep(200L * (it + 1))
            }
            val order = client.get<Order>(getCloudUrl("v1/orders/$orderId")) {
                addCloudHeaders(this, session)
            }
            if (order.orderStatus == "Completed") {
                Log.debug("Order confirmed in ${it + 1} tries")
                return true
            }
        }
        return false
    }

}

inline fun <reified T> ensureData(data: T?, name: String): T {
    return data ?: throw ServiceException(ServiceException.Type.MISSING_DATA, "No $name")
}