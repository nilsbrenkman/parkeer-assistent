package nl.parkeerassistent

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

    val log = Logger.getLogger("Service")

    fun info(method: Method, message: String) {
        log.info(logMessage(method, message))
    }
    fun warn(method: Method, message: String) {
        log.warn(logMessage(method, message))
    }

    private fun logMessage(method: Method, message: String): String {
        return String.format("[%s] [%s] - %s", method.service().name, method.method(), message)
    }

}