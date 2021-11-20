package nl.parkeerassistent.monitoring

import io.ktor.application.ApplicationCall
import org.apache.log4j.Logger

object Monitoring {

    enum class Service {
        Login,
        User,
        Visitor,
        Parking,
        Payment,
        ;
    }

    interface Method {
        fun service(): Service
        fun method(): String
    }

    enum class Level {
        INFO,
        WARN,
        ERROR,
        ;
    }

    val log = Logger.getLogger("AnalyticEvent")

    fun info(call: ApplicationCall, method: Method, message: String) {
        ES.log(call, method, Level.INFO, message)
        log.info(logMessage("INFO", method, message))
    }
    fun warn(call: ApplicationCall, method: Method, message: String) {
        ES.log(call, method, Level.WARN, message)
        log.warn(logMessage("WARN", method, message))
    }

    fun error(call: ApplicationCall, method: Method, message: String) {
        ES.log(call, method, Level.ERROR, message)
        log.error(logMessage("WARN", method, message))
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
