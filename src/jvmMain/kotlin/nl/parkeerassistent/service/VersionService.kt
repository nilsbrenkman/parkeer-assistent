package nl.parkeerassistent.service

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

object VersionService {

    private val activeVersions = listOf("1.0.0","1.0.1","1.0.2","1.0.3","1.0.4","1.0.5")

    suspend fun version(call: ApplicationCall) {
        val version = call.parameters["version"]!!
        if (activeVersions.contains(version)) {
            call.respondText("live", status = HttpStatusCode.OK)
        } else {
            call.respondText("mock", status = HttpStatusCode.NotFound)
        }
    }

}