
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.HttpsRedirect
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.features.gzip
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.HTML
import nl.parkeerassistent.html.application
import nl.parkeerassistent.html.feedback
import nl.parkeerassistent.html.open
import nl.parkeerassistent.model.AddParkingRequest
import nl.parkeerassistent.model.AddVisitorRequest
import nl.parkeerassistent.model.BalanceResponse
import nl.parkeerassistent.model.HistoryResponse
import nl.parkeerassistent.model.IdealResponse
import nl.parkeerassistent.model.LoginRequest
import nl.parkeerassistent.model.ParkingResponse
import nl.parkeerassistent.model.PaymentRequest
import nl.parkeerassistent.model.PaymentResponse
import nl.parkeerassistent.model.RegimeResponse
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.model.StatusResponse
import nl.parkeerassistent.model.UserResponse
import nl.parkeerassistent.model.VisitorResponse
import nl.parkeerassistent.service.LoginService
import nl.parkeerassistent.service.ParkingService
import nl.parkeerassistent.service.PaymentService
import nl.parkeerassistent.service.ServiceUtil
import nl.parkeerassistent.service.UserService
import nl.parkeerassistent.service.VersionService
import nl.parkeerassistent.service.VisitorService
import nl.parkeerassistent.style.Style
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.io.File

fun main() {
    org.apache.log4j.BasicConfigurator.configure()
    if ("true" != System.getenv("DEBUG_LOG")) {
        Logger.getRootLogger().level = Level.INFO
    }

    val log = Logger.getLogger("Server.kt")

    val trustStore = System.getenv("TRUST_STORE")
    val trustStoreFile = File(trustStore)
    if (trustStoreFile.exists()) {
        log.info("Using trust store: ${trustStoreFile.absolutePath}")
        System.setProperty("javax.net.ssl.trustStore", trustStoreFile.absolutePath)
        System.setProperty("javax.net.ssl.trustStorePassword", "parkeerassistent")
    } else {
        log.info("Trust store not found: ${trustStoreFile.absolutePath}")
    }

    val host = System.getenv("HOST")
    val port = System.getenv("PORT").toInt()

    log.info("Starting server: $host:$port")

    embeddedServer(Netty, port = port, host = host) {
        install(ContentNegotiation) {
            json()
        }
        install(Compression) {
            gzip()
        }
        if ("true" == System.getenv("FORCE_SSL")) {
            install(XForwardedHeaderSupport)
            install((HttpsRedirect))
        }
        routing {
            get("/version/{version}") {
                VersionService.version(call)
            }
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::application)
            }
            get("/open") {
                call.respondHtml(HttpStatusCode.OK, HTML::open)
            }
            get("/feedback") {
                call.respondHtml(HttpStatusCode.OK, HTML::feedback)
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
                get("/history") {
                    call.respond(
                        ServiceUtil.execute(
                            ParkingService.Method.History,
                            call,
                            HistoryResponse(emptyList()),
                            ParkingService::history
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