
import io.ktor.application.ApplicationCallPipeline
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
import kotlinx.coroutines.coroutineScope
import kotlinx.html.HTML
import nl.parkeerassistent.html.application
import nl.parkeerassistent.html.completeMockPayment
import nl.parkeerassistent.html.feedback
import nl.parkeerassistent.mock.mock
import nl.parkeerassistent.mock.mockRouting
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
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    org.apache.log4j.BasicConfigurator.configure()
    if ("true" != System.getenv("DEBUG_LOG")) {
        Logger.getRootLogger().level = Level.INFO
    }
    val log = LoggerFactory.getLogger("Server.kt")

    val port = System.getenv("PORT").toInt()
    log.info("Starting server: $port")

    val mockBuilds = System.getenv("MOCK_BUILDS")

    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            json()
        }
        install(Compression) {
            gzip()
        }
        if ("true" == System.getenv("FORCE_SSL")) {
            install(XForwardedHeaderSupport)
            install(HttpsRedirect)
        }
        intercept(ApplicationCallPipeline.Monitoring) {
            val context = this
            try {
                coroutineScope {
                    context.proceed()
                }
            } catch (exception: Throwable) {
                if (this.call.response.status() != null) {
                    context.call.respondText(text = "")
                }
            }
        }
        routing {
            get("/version/{version}") {
                VersionService.version(call)
            }
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::application)
            }
            get("/completeMockPayment") {
                call.respondHtml(HttpStatusCode.OK, HTML::completeMockPayment)
            }
            get("/feedback") {
                call.respondHtml(HttpStatusCode.OK, HTML::feedback)
            }
            post("/") {
                call.respondRedirect("/", false)
            }
            mock {
                mockRouting()
            }
            get("/login") {
                call.respond(
                    ServiceUtil.execute(
                        LoginService.Method.LoggedIn,
                        call,
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
                        LoginService::login
                    )
                )
            }
            get("/logout") {
                call.respond(
                    ServiceUtil.execute(
                        LoginService.Method.Logout,
                        call,
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
                            UserService::get
                        )
                    )
                }
                get("/balance") {
                    call.respond(
                        ServiceUtil.execute(
                            UserService.Method.Balance,
                            call,
                            UserService::balance
                        )
                    )
                }
                get("/regime/{date}") {
                    call.respond(
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
                    call.respond(
                        ServiceUtil.execute(
                            ParkingService.Method.Get,
                            call,
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
                            ParkingService::start
                        )
                    )
                }
                delete("/{id}") {
                    call.respond(
                        ServiceUtil.execute(
                            ParkingService.Method.Stop,
                            call,
                            ParkingService::stop
                        )
                    )
                }
                get("/history") {
                    call.respond(
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
                    call.respond(
                        ServiceUtil.execute(
                            VisitorService.Method.Get,
                            call,
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
                            VisitorService::add
                        )
                    )
                }
                delete("/{visitorId}") {
                    call.respond(
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
                    call.respond(
                        ServiceUtil.execute(
                            PaymentService.Method.Ideal,
                            call,
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
                            PaymentService::payment
                        )
                    )
                }
                post("/complete") {
                    val request = call.receive<CompleteRequest>()
                    call.respond(
                        ServiceUtil.execute(
                            PaymentService.Method.Complete,
                            call,
                            request,
                            PaymentService::complete
                        )
                    )
                }
                get("/{transactionId}") {
                    call.respond(
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

