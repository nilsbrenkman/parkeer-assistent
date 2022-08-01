package nl.parkeerassistent

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.features.HttpResponseValidator
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import kotlinx.serialization.json.Json
import nl.parkeerassistent.service.ServiceException

object ApiHelper {

    val baseUrl = "https://aanmeldenparkeren.amsterdam.nl/"

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

    fun getUrl(url: String):String {
        return baseUrl + url
    }

    fun addHeaders(httpRequestBuilder: HttpRequestBuilder,
                   session: Session,
                   referer: String = "Customer/Dashboard") {
        httpRequestBuilder.header("Cookie", session.header())
        httpRequestBuilder.header("Host", "aanmeldenparkeren.amsterdam.nl")
        httpRequestBuilder.header("Referer", baseUrl + referer)
        httpRequestBuilder.header("Cache-Control", "no-cache")
    }

}