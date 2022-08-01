import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import nl.parkeerassistent.DateUtil
import nl.parkeerassistent.monitoring.DateRange
import nl.parkeerassistent.monitoring.ES
import nl.parkeerassistent.monitoring.Query
import nl.parkeerassistent.monitoring.QueryRequest
import nl.parkeerassistent.monitoring.QueryResponse
import nl.parkeerassistent.monitoring.Range
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

fun main() {
    org.apache.log4j.BasicConfigurator.configure()
    if ("true" != System.getenv("DEBUG_LOG")) {
        Logger.getRootLogger().level = Level.INFO
    }

    val log = Logger.getLogger("ElasticCleaner.kt")

    val trustStore = System.getenv("TRUST_STORE")
    val trustStoreFile = File(trustStore)
    if (trustStoreFile.exists()) {
        log.info("Using trust store: ${trustStoreFile.absolutePath}")
        System.setProperty("javax.net.ssl.trustStore", trustStoreFile.absolutePath)
        System.setProperty("javax.net.ssl.trustStorePassword", "parkeerassistent")
    } else {
        log.info("Trust store not found: ${trustStoreFile.absolutePath}")
    }

    val retention = System.getenv("ELASTIC_SEARCH_RETENTION").toLong()
    val deleteBefore: Instant = Instant.now().minus(retention, ChronoUnit.DAYS)
    log.info("Deleting documents before ${DateUtil.dateFormatter.format(deleteBefore)}")

    val query = QueryRequest(
        query = Query(
            range = Range(
                date = DateRange(
                    lte = DateUtil.dateFormatter.format(deleteBefore)
                )
            )
        )
    )

    runBlocking {
        send(query)
    }
}

suspend fun send(query: QueryRequest): Boolean {

    val log = Logger.getLogger("ElasticCleaner.kt")

    try {
        val response = ES.client.post<QueryResponse>(ES.url + "/" + ES.index + "/_delete_by_query") {
            contentType(ContentType.Application.Json)
            header("Authorization", ES.basicAuth)
            body = query
        }
        log.info("Deleted ${response.deleted} documents")
        return true
    } catch (e: Exception) {
        log.warn("Cleanup failed", e)
    }

    return false
}