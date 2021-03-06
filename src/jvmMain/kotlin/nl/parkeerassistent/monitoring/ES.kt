package nl.parkeerassistent.monitoring

import io.ktor.application.ApplicationCall
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.URLBuilder
import kotlinx.serialization.json.Json
import nl.parkeerassistent.DateUtil
import org.apache.log4j.Logger
import java.util.Base64
import java.util.Date
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object ES {

    private val log = Logger.getLogger("ES")

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
        val date = DateUtil.dateTime.format(Date())
        val os = call.request.headers["PA-OS"] ?: "null"
        val sdk = call.request.headers["PA-SDK"] ?: "null"
        val userId = (if (os == "Web") call.request.cookies["userid"] else call.request.headers["PA-UserId"]) ?: "null"
        val version = call.request.headers["PA-Version"] ?: "null"
        val build = call.request.headers["PA-Build"] ?: "0"
        val event = Event(date, userId.lowercase(), os, sdk, version, build.toInt(), method.service().name, method.method(), level.name, message)
        try {
            executor.submit(ESEvent(event))
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