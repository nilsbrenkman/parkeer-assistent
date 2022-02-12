package nl.parkeerassistent.android.service

import android.content.Context
import android.os.Build
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.parkeerassistent.android.BuildConfig
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.util.Preference
import nl.parkeerassistent.android.util.getPreference
import nl.parkeerassistent.android.util.getPreferenceOrDefault
import nl.parkeerassistent.android.util.setPreference
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*
import javax.inject.Singleton

class ApiClient(val applicationContext: Context) {

    val client = OkHttpClient()
    val contentType = "application/json; charset=utf-8".toMediaType()
    val baseUrl: String = BuildConfig.BASE_URL
    val sessionCookies: MutableMap<Cookie.Type, Cookie> = EnumMap(Cookie.Type::class.java)

    val mock: Boolean by lazy {
        val myVersion = applicationContext.getPreference(Preference.VERSION)
        val appVersion = BuildConfig.VERSION_NAME

        if (myVersion == appVersion) false else runBlocking {
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(this@ApiClient.baseUrl + "version/" + appVersion).get().build()
                try {
                    this@ApiClient.client.newCall(request).execute().use { response ->
                        if (response.code == 200) {
                            applicationContext.setPreference(Preference.VERSION, appVersion)
                        }
                        return@withContext response.code == 404
                    }
                } catch (e: Exception) {
                    Log.e("ApiClient", e.message ?: "Exception while trying to check version")
                }
                false
            }
        }
    }

    val analytics: List<Pair<String, String>>

    init {
        Cookie.Type.values().forEach { type ->
            applicationContext.getPreference(type.preference)?.let { value ->
                val cookie = Cookie(type, value)
                sessionCookies[cookie.type] = cookie
            }
        }
        val uuid = applicationContext.getPreferenceOrDefault(Preference.UUID) { UUID.randomUUID().toString() }
        analytics = listOf(
            "PA-UserId" to uuid,
            "PA-OS" to "Android",
            "PA-SDK" to Build.VERSION.SDK_INT.toString(),
            "PA-Version" to BuildConfig.VERSION_NAME,''
            "PA-Build" to BuildConfig.VERSION_CODE.toString()
        )
    }

    fun updateCookie(cookie: Cookie) {
        val current = sessionCookies[cookie.type]
        if (current == null || current.value != cookie.value) {
            sessionCookies[cookie.type] = cookie
            applicationContext.setPreference(cookie.type.preference, cookie.value)
        }
    }

    @Throws(ApiException::class)
    inline fun <reified R> get(path: String): R {
        return call(path, Method.GET)
    }

    @Throws(ApiException::class)
    inline fun <reified D, reified R> post(path: String, data: D): R {
        return call(path, Method.POST, Json.encodeToString(data))
    }

    @Throws(ApiException::class)
    inline fun <reified R> delete(path: String): R {
        return call(path, Method.DELETE)
    }

    @Throws(ApiException::class)
    inline fun <reified R> call(path: String, method: Method, data: String? = null): R {
        val body = data?.toRequestBody(contentType)
        val request = Request.Builder()
            .url(baseUrl + path)
            .method(method.name, body)
            .cookies(sessionCookies.values.toList())
            .analytics(analytics)
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val cookies = response.headers.values("Set-Cookie")
                cookies.forEach { header ->
                    Cookie.parse(header)?.let(this::updateCookie)
                }
                if (response.code / 100 > 2) {
                    throw ServiceException(response.code, response.message)
                }
                val responseBody = response.body?.string() ?: throw NoContentException()
                return Json.decodeFromString(responseBody)
            }
        } catch (e: IOException) {
            throw NetworkException(e)
        }
    }

    enum class Method {
        GET,
        POST,
        DELETE,
        ;
    }

}

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Singleton
    @Provides
    fun provide(@ApplicationContext applicationContext: Context) : ApiClient {
        return ApiClient(applicationContext)
    }
}

class Cookie(val type: Type, val value: String) {

    companion object {
        fun parse(cookie: String): Cookie? {
            val separator = cookie.indexOf('=')
            if (separator < 1) return null
            val name = cookie.substring(0, separator)
            val type = Type.values().firstOrNull { type -> type.key == name }
            return type?.get(cookie.substring(separator + 1))
        }
    }

    enum class Type(val key: String, val preference: Preference) {
        SESSION ("session", Preference.COOKIE_SESSION),
        CUSTOMER("customerid", Preference.COOKIE_CUSTOMER),
        PERMIT  ("permitid", Preference.COOKIE_PERMIT),
        ;
        fun get(value: String): Cookie {
            return Cookie(this, value)
        }
    }

    override fun toString(): String {
        return "${type.key}=$value"
    }

}

fun Request.Builder.cookies(cookies: List<Cookie>) = apply {
    cookies.forEach { cookie ->
        addHeader("Cookie", cookie.toString())
    }
}

fun Request.Builder.analytics(analytics: List<Pair<String, String>>) = apply {
    analytics.forEach { analytic ->
        addHeader(analytic.first, analytic.second)
    }
}