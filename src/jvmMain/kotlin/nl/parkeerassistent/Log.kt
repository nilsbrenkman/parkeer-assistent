package nl.parkeerassistent

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.log4j.Logger

object Log {

    val log = Logger.getLogger("General")

    fun debug(msg: String) {
        log.debug(msg)
    }

    inline fun <reified T> json(key: String, obj: T) {
        log.debug("${key}: ${Json.encodeToString(obj)}")
    }

}