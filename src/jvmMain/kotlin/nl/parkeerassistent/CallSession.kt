package nl.parkeerassistent

import io.ktor.application.ApplicationCall
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.features.HttpResponseValidator
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.parkeerassistent.service.LoginService
import nl.parkeerassistent.service.ServiceException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Base64

class CallSession(val call: ApplicationCall) {

    val cookieStore: PassthroughCookieStorage = PassthroughCookieStorage(call.request.cookies["session"])

    var user: User? = call.request.cookies["customerid"]?.let { decodeCookie(it) }
        set(value) {
            if (field != value) {
                updateCookie("customerid", value)
                field = value
            }
        }

    var permit: Permit? = call.request.cookies["permitid"]?.let { decodeCookie(it) }
        set(value) {
            updateCookie("permitid", value)
            field = value
        }

    val client: HttpClient = HttpClient(Java) {
        followRedirects = false
        engine {
            threadsCount = 5
        }
        install(JsonFeature) {
            serializer = CallSession.serializer
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpCookies) {
            storage = cookieStore
        }
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status.value / 100 != 2) {
                    throw ServiceException(ServiceException.Type.API, "Request failed [status=${response.status.value}]")
                }
            }
        }
    }

    init {
        user?.token?.let {
            val jwt = JWT(it)
            if (jwt.isExpired()) {
                Log.debug("refreshing token")
                runBlocking {
                    LoginService.isLoggedIn(this@CallSession)
                }
            }
        }
    }

    suspend fun updateSessionCookie() {
        cookieStore.createSetCookie()?.let {
            if (it.isEmpty()) call.response.cookies.appendExpired("session") else
                call.response.cookies.append("session", it)
        }
    }

    companion object {
        val serializer = KotlinxSerializer(Json {
            isLenient = false
            ignoreUnknownKeys = true
            allowSpecialFloatingPointValues = true
            useArrayPolymorphism = false
        })
    }

    private inline fun <reified T> decodeCookie(cookie: String): T? {
        try {
            val json = String(Base64.getDecoder().decode(cookie), StandardCharsets.UTF_8)
            return Json.decodeFromString(json)
        } catch (e: SerializationException) {
            Log.debug("Unable to decode cookie: ${cookie}")
            return null
        }
    }

    private inline fun <reified T> updateCookie(key: String, cookie: T?) {
        cookie?.let {
            val json = Json.encodeToString(it)
            val value = String(
                Base64.getUrlEncoder().encode(ByteBuffer.wrap(json.toByteArray(StandardCharsets.UTF_8))).array(),
                StandardCharsets.UTF_8
            )
            call.response.cookies.append(key,  value)
        } ?: call.response.cookies.appendExpired(key)
    }

}

@Serializable
data class User(
    val token: String
)

@Serializable
data class Permit(
    val reportCode: Long,
    val paymentZoneId: String?
)

