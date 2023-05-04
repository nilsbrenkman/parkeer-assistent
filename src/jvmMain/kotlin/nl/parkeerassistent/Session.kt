package nl.parkeerassistent

import org.jsoup.Connection
import org.jsoup.nodes.Document
import kotlin.collections.set

class Session(cookieString: String?) {

    val cookies: MutableMap<String, String> = LinkedHashMap()
        get() = field

    init {
        if (cookieString != null) {
            addCookies(cookieString.split("; "))
        }
    }

    fun send(connection: Connection): Document {
        connection.cookies(cookies)
        connection.execute()
        cookies.putAll(connection.request().cookies())
        if (connection.response().hasHeader("Set-Cookie")) {
            addCookies(connection.response().headers("Set-Cookie"))
        }
        return connection.response().parse()
    }

    fun header(): String {
        return cookies.entries.map { "${it.key}=${it.value}" }.joinToString("; ")
    }

    private fun addCookies(cookieList: List<String>) {
        for (cookie in cookieList) {
            val split = cookie.indexOf("=")
            if (split == -1) {
                continue
            }
            val key = cookie.substring(0, split)
            val value = cookie.substring(split + 1)
            cookies[key] = value
        }
    }

}