package nl.parkeerassistent

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json

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
    }

    fun getUrl(url: String):String {
        return baseUrl + url
    }

    fun addHeaders(httpRequestBuilder: HttpRequestBuilder, session: Session) {
        httpRequestBuilder.header("Cookie", session.header())
        httpRequestBuilder.header("Host", "aanmeldenparkeren.amsterdam.nl")
        httpRequestBuilder.header("Referer", baseUrl + "Customer/Dashboard")
        httpRequestBuilder.header("Cache-Control", "no-cache")
    }

}