package nl.parkeerassistent.monitoring

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.apache.log4j.Logger

class ESEvent(private val event: Event) : Runnable {

    companion object {
        private val log = Logger.getLogger("ESEvent")
    }

    override fun run() {
        runBlocking {
            send()
        }
    }

    suspend fun send() {
        try {
            val response = ES.client.post<EventResponse>(ES.url + "/" + ES.index + "/event") {
                contentType(ContentType.Application.Json)
                header("Authorization", ES.basicAuth)
                body = event
            }
            if (response.result != "created") {
                log.warn("Event creation failed")
            }
        } catch (e: Exception) {
            log.warn("Sending failed", e)
        }

    }

}