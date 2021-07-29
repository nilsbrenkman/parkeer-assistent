package nl.parkeerassistent

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nl.parkeerassistent.model.Visitor
import org.apache.log4j.Logger

object Monitoring {

    enum class Service {
        Login,
        User,
        Visitor,
        Parking,
        ;
    }

    interface Method {
        fun service(): Service
        fun method(): String
    }

    val log = Logger.getLogger("AnalyticEvent")

    fun info(method: Method, message: String) {
        log.info(logMessage("INFO", method, message))
    }
    fun warn(method: Method, message: String) {
        log.warn(logMessage("WARN", method, message))
    }

    private fun logMessage(severity: String, method: Method, message: String): String {
        val params = mapOf(
            "severity" to severity,
            "service" to method.service().name,
            "method" to method.method(),
            "message" to message
        )
        return params.entries.joinToString(",", "[[{", "}]]") {
            "\"" + it.key + "\":\"" + it.value + "\""
        }
    }

}
