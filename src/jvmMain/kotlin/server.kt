
import io.ktor.application.ApplicationCall
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
import io.ktor.http.content.file
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
import nl.parkeerassistent.model.CompleteRequest
import nl.parkeerassistent.model.LoginRequest
import nl.parkeerassistent.model.PaymentRequest
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
//        log.info("Using trust store: ${trustStoreFile.absolutePath}")
//        System.setProperty("javax.net.ssl.trustStore", trustStoreFile.absolutePath)
//        System.setProperty("javax.net.ssl.trustStorePassword", "parkeerassistent")
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
                call.respondOrNull(
                    ServiceUtil.execute(
                        LoginService.Method.LoggedIn,
                        call,
                        LoginService::isLoggedIn
                    )
                )
            }
            post("/login") {
                val request = call.receive<LoginRequest>()
                call.respondOrNull(
                    ServiceUtil.execute(
                        LoginService.Method.Login,
                        call,
                        request,
                        LoginService::login
                    )
                )
            }
            get("/logout") {
                call.respondOrNull(
                    ServiceUtil.execute(
                        LoginService.Method.Logout,
                        call,
                        LoginService::logout
                    )
                )
            }
            route("/user") {
                get {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            UserService.Method.Get,
                            call,
                            UserService::get
                        )
                    )
                }
                get("/balance") {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            UserService.Method.Balance,
                            call,
                            UserService::balance
                        )
                    )
                }
                get("/regime/{date}") {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            UserService.Method.Regime,
                            call,
                            UserService::regime
                        )
                    )
                }
            }
            route("/parking") {
                get {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            ParkingService.Method.Get,
                            call,
                            ParkingService::get
                        )
                    )
                }
                post {
                    val request = call.receive<AddParkingRequest>()
                    call.respondOrNull(
                        ServiceUtil.execute(
                            ParkingService.Method.Start,
                            call,
                            request,
                            ParkingService::start
                        )
                    )
                }
                delete("/{id}") {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            ParkingService.Method.Stop,
                            call,
                            ParkingService::stop
                        )
                    )
                }
                get("/history") {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            ParkingService.Method.History,
                            call,
                            ParkingService::history
                        )
                    )
                }
            }
            route("/visitor") {
                get {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            VisitorService.Method.Get,
                            call,
                            VisitorService::get
                        )
                    )
                }
                post {
                    val request = call.receive<AddVisitorRequest>()
                    call.respondOrNull(
                        ServiceUtil.execute(
                            VisitorService.Method.Add,
                            call,
                            request,
                            VisitorService::add
                        )
                    )
                }
                delete("/{visitorId}") {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            VisitorService.Method.Delete,
                            call,
                            VisitorService::delete
                        )
                    )
                }
            }
            route("/payment") {
                get {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            PaymentService.Method.Ideal,
                            call,
                            PaymentService::ideal
                        )
                    )
                }
                post {
                    val request = call.receive<PaymentRequest>()
                    call.respondOrNull(
                        ServiceUtil.execute(
                            PaymentService.Method.Payment,
                            call,
                            request,
                            PaymentService::payment
                        )
                    )
                }
                post("/complete") {
                    val request = call.receive<CompleteRequest>()
                    call.respondOrNull(
                        ServiceUtil.execute(
                            PaymentService.Method.Complete,
                            call,
                            request,
                            PaymentService::complete
                        )
                    )
                }
                get("/{transactionId}") {
                    call.respondOrNull(
                        ServiceUtil.execute(
                            PaymentService.Method.Status,
                            call,
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
            static("/.well-known") {
                file("apple-app-site-association", File(System.getenv("APPLE_APP_SITE_ASSOCIATION")))
            }
        }
    }.start(wait = true)
}


suspend inline fun <reified T : Any> ApplicationCall.respondOrNull(message: T?) {
    if (message != null) {
        this.respond(message)
    }
}

