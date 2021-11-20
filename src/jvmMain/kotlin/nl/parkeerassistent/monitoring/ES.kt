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

    val index = "parkeer-assistent"

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
        val config = System.getProperty("es.url", "https://username:password@elasticsearch.host")
        val urlBuilder = URLBuilder(config)
        url = urlBuilder.protocol.name + "://" + urlBuilder.host
        basicAuth = "Basic " + Base64.getEncoder().encodeToString((urlBuilder.user!! + ":" + urlBuilder.password!!).toByteArray(Charsets.UTF_8))
    }
    fun log(call: ApplicationCall, method: Monitoring.Method, level: Monitoring.Level, message: String) {
        val date = DateUtil.dateTime.format(Date())
        val userId = call.request.headers["PA-UserId"]
        val os = call.request.headers["PA-OS"]
        val build = call.request.headers["PA-Build"]
        if (listOf(userId, os, build).any { it == null }) return
        val event = Event(date, userId!!, os!!, build!!.toInt(), method.service().name, method.method(), level.name, message)
        try {
            executor.submit(ESEvent(event))
        } catch (e: RejectedExecutionException) {
            log.warn("Unable to submit event", e)
        }
    }

    fun shutdown() {
        executor.shutdown()
    }

}