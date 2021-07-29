import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.css.CSSBuilder
import kotlinx.css.body
import kotlinx.html.*
import nl.parkeerassistent.*
import nl.parkeerassistent.model.AddParkingRequest
import nl.parkeerassistent.model.AddVisitorRequest
import nl.parkeerassistent.model.LoginRequest
import nl.parkeerassistent.style.Style
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.io.File

fun HTML.index() {
    head {
        title("Parkeer Assistent")
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css"
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "/static/style.css"
        }
        meta {
            name = "viewport"
            content = "width=375, initial-scale=1"
        }
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/ParkeerAssistent.js") {}
    }
}


fun main() {
    org.apache.log4j.BasicConfigurator.configure()
    if (System.getProperty("log.debug", "false") != "true") {
        Logger.getRootLogger().level = Level.INFO
    }

    val log = Logger.getLogger("Server.kt")

    val trustStore = System.getProperty("server.trustStore", "keystore.jks")
    val trustStoreFile = File(trustStore)
    if (trustStoreFile.exists()) {
        log.info("Using trust store: ${trustStoreFile.absolutePath}")
        System.setProperty("javax.net.ssl.trustStore", trustStoreFile.absolutePath)
        System.setProperty("javax.net.ssl.trustStorePassword", "parkeerassistent")
    } else {
        log.info("Trust store not found: ${trustStoreFile.absolutePath}")
    }

    val host = System.getProperty("server.host", "127.0.0.1")
    val port = System.getProperty("server.port", "3000").toInt()

    log.info("Starting server: $host:$port")

    embeddedServer(Netty, port = port, host = host) {
        install(ContentNegotiation) {
            json()
        }
        install(Compression) {
            gzip()
        }
        if ("true" == System.getProperty("server.forceSsl")) {
            install(XForwardedHeaderSupport)
            install((HttpsRedirect))
        }
        routing {
            get("/version/{version}") {
                VersionService.version(call)
            }
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            post("/") {
                call.respondRedirect("/", false)
            }
            get("/login") {
                call.respond(LoginService.isLoggedIn(call.request))
            }
            post("/login") {
                val request = call.receive<LoginRequest>()
                call.respond(LoginService.login(request, call))
            }
            get("/logout") {
                call.respond(LoginService.logout(call))
            }
            route("/user") {
                get {
                    UserService.get(call)
                }
                get("/balance") {
                    UserService.balance(call)
                }
            }
            route("/parking") {
                get {
                    ParkingService.get(call)
                }
                post {
                    val request = call.receive<AddParkingRequest>()
                    ParkingService.start(request, call)
                }
                delete("/{id}") {
                    ParkingService.stop(call)
                }
            }
            route("/visitor") {
                get {
                    call.respond(VisitorService.get(call))
                }
                post {
                    val request = call.receive<AddVisitorRequest>()
                    call.respond(VisitorService.add(request, call))
                }
                delete("/{visitorId}") {
                    call.respond(VisitorService.delete(call))
                }
            }
            static("/static") {
                resources()
                get("/style.css") {
                    call.respondText(Style.toString(), ContentType.Text.CSS)
                }
            }
        }
    }.start(wait = true)
}