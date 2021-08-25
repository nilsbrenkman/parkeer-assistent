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
import kotlinx.html.*
import nl.parkeerassistent.*
import nl.parkeerassistent.model.*
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
                call.respond(
                    ServiceUtil.execute(
                        LoginService.Method.LoggedIn,
                        call,
                        Response(false),
                        LoginService::isLoggedIn
                    )
                )
            }
            post("/login") {
                val request = call.receive<LoginRequest>()
                call.respond(
                    ServiceUtil.execute(
                        LoginService.Method.Login,
                        call,
                        request,
                        Response(false),
                        LoginService::login
                    )
                )
            }
            get("/logout") {
                call.respond(
                    ServiceUtil.execute(
                        LoginService.Method.Logout,
                        call,
                        Response(false),
                        LoginService::logout
                    )
                )
            }
            route("/user") {
                get {
                    call.respond(
                        ServiceUtil.execute(
                            UserService.Method.Get,
                            call,
                            UserResponse("", 0.0, "",""),
                            UserService::get
                        )
                    )
                }
                get("/balance") {
                    call.respond(
                        ServiceUtil.execute(
                            UserService.Method.Balance,
                            call,
                            BalanceResponse(""),
                            UserService::balance
                        )
                    )
                }
                get("/regime/{date}") {
                    call.respond(
                        ServiceUtil.execute(
                            UserService.Method.Regime,
                            call,
                            RegimeResponse("", ""),
                            UserService::regime
                        )
                    )
                }
            }
            route("/parking") {
                get {
                    call.respond(
                        ServiceUtil.execute(
                            ParkingService.Method.Get,
                            call,
                            ParkingResponse(emptyList(), emptyList()),
                            ParkingService::get
                        )
                    )
                }
                post {
                    val request = call.receive<AddParkingRequest>()
                    call.respond(
                        ServiceUtil.execute(
                            ParkingService.Method.Start,
                            call,
                            request,
                            Response(false),
                            ParkingService::start
                        )
                    )
                }
                delete("/{id}") {
                    call.respond(
                        ServiceUtil.execute(
                            ParkingService.Method.Stop,
                            call,
                            Response(false),
                            ParkingService::stop
                        )
                    )
                }
            }
            route("/visitor") {
                get {
                    call.respond(
                        ServiceUtil.execute(
                            VisitorService.Method.Get,
                            call,
                            VisitorResponse(emptyList()),
                            VisitorService::get
                        )
                    )
                }
                post {
                    val request = call.receive<AddVisitorRequest>()
                    call.respond(
                        ServiceUtil.execute(
                            VisitorService.Method.Add,
                            call,
                            request,
                            VisitorResponse(emptyList()),
                            VisitorService::add
                        )
                    )
                }
                delete("/{visitorId}") {
                    call.respond(
                        ServiceUtil.execute(
                            VisitorService.Method.Delete,
                            call,
                            Response(false),
                            VisitorService::delete
                        )
                    )
                }
            }
            route("/payment") {
                get {
                    call.respond(
                        ServiceUtil.execute(
                            PaymentService.Method.Ideal,
                            call,
                            IdealResponse(emptyList(), emptyList()),
                            PaymentService::ideal
                        )
                    )
                }
                post {
                    val request = call.receive<PaymentRequest>()
                    call.respond(
                        ServiceUtil.execute(
                            PaymentService.Method.Payment,
                            call,
                            request,
                            PaymentResponse("", ""),
                            PaymentService::payment
                        )
                    )
                }
                get("/{transactionId}") {
                    call.respond(
                        ServiceUtil.execute(
                            PaymentService.Method.Status,
                            call,
                            StatusResponse("error"),
                            PaymentService::status
                        )
                    )
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