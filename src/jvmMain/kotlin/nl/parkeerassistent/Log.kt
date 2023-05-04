package nl.parkeerassistent

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.logging.Logger

object Log {

    val log = Logger.getLogger("Debug")

    fun debug(msg: String) {
        log.info(msg)
    }

    inline fun <reified T> json(key: String, obj: T) {
        log.info("${key}: ${Json.encodeToString(obj)}")
    }

}