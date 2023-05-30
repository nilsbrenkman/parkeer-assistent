package nl.parkeerassistent.monitoring

import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.css.body
import kotlinx.css.header
import org.apache.log4j.Logger

class ESEvent(private val event: Event, private val index: String) : Runnable {

    companion object {
        private val log = Logger.getLogger("ESEvent")
    }

    private var retries = 0

    override fun run() {
        val success = runBlocking {
            send()
        }
        if (! success) {
            ES.retry(this)
        }
    }

    suspend fun send(): Boolean {
        try {
            val response = ES.client.post<EventResponse>(ES.url + "/" + index + "/event") {
                contentType(ContentType.Application.Json)
                header("Authorization", ES.basicAuth)
                body = event
            }
            if (response.result == "created") {
                return true
            }
            log.warn("Event creation failed")
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) {
                log.warn("Creating index: $index")
                ES.client.put<String>(ES.url + "/" + index) {
                    header("Authorization", ES.basicAuth)
                }
            } else {
                log.warn("Sending failed", e)
            }
        } catch (e: Exception) {
            log.warn("Sending failed", e)
        }
        return false
    }

    fun retry(): Boolean {
        if (retries < 5) {
            retries++
            return true
        }
        return false
    }

}