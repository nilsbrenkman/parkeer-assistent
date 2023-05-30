package nl.parkeerassistent

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger

inline fun <reified T> Logger.json(key: String, obj: T) {
    debug("${key}: ${Json.encodeToString(obj)}")
}
