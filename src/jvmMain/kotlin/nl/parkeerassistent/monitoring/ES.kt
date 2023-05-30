package nl.parkeerassistent.monitoring

import io.ktor.application.ApplicationCall
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.URLBuilder
import kotlinx.serialization.json.Json
import nl.parkeerassistent.DateUtil
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Base64
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object ES {

    private val log = LoggerFactory.getLogger(ES::class.java)

    val url: String
    val basicAuth: String

    val index = System.getenv("ELASTIC_SEARCH_INDEX")

    private val executor = ThreadPoolExecutor(1, 10, 1, TimeUnit.MINUTES, LinkedBlockingQueue(1000))

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

    init {
        val config = System.getenv("BONSAI_URL")
        val urlBuilder = URLBuilder(config)
        url = urlBuilder.protocol.name + "://" + urlBuilder.host
        basicAuth = "Basic " + Base64.getEncoder().encodeToString((urlBuilder.user!! + ":" + urlBuilder.password!!).toByteArray(Charsets.UTF_8))
    }

    fun log(call: ApplicationCall, method: Monitoring.Method, level: Monitoring.Level, message: String) {
        val instant = Instant.now()
        val timestamp = DateUtil.gmtDateFormatter.format(instant)
        val os = call.request.headers["X-ParkeerAssistent-OS"] ?: "null"
        val sdk = call.request.headers["X-ParkeerAssistent-SDK"] ?: "null"
        val userId = (if (os == "Web") call.request.cookies["userid"] else call.request.headers["X-ParkeerAssistent-UserId"]) ?: "null"
        val version = call.request.headers["X-ParkeerAssistent-Version"] ?: "null"
        val build = call.request.headers["X-ParkeerAssistent-Build"] ?: "0"
        val event = Event(timestamp, userId.lowercase(), os, sdk, version, build.toInt(), method.service().name, method.method(), level.name, message)

        val index = "parkeerassistent-" + DateUtil.esIndexFormatter.format(instant)
        try {
            executor.submit(ESEvent(event, index))
        } catch (e: RejectedExecutionException) {
            log.warn("Unable to submit event", e)
        }
    }

    fun retry(event: ESEvent) {
        if (event.retry()) {
            executor.submit(event)
        }
    }

    fun shutdown() {
        executor.shutdown()
    }

}