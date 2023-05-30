package nl.parkeerassistent.mock

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import nl.parkeerassistent.DateUtil
import nl.parkeerassistent.ensureData
import nl.parkeerassistent.model.AddParkingRequest
import nl.parkeerassistent.model.AddVisitorRequest
import nl.parkeerassistent.model.BalanceResponse
import nl.parkeerassistent.model.CompleteRequest
import nl.parkeerassistent.model.HistoryResponse
import nl.parkeerassistent.model.IdealResponse
import nl.parkeerassistent.model.Issuer
import nl.parkeerassistent.model.LoginRequest
import nl.parkeerassistent.model.ParkingResponse
import nl.parkeerassistent.model.PaymentRequest
import nl.parkeerassistent.model.RegimeResponse
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.model.UserResponse
import nl.parkeerassistent.model.VisitorResponse
import javax.naming.NoPermissionException

fun Route.mockRouting() {
    get("/login") {
        call.respond(Response(MockStateContainer.mock().user.loggedIn, ""))
    }
    post("/login") {
        val request = call.receive<LoginRequest>()
        if (request.username.lowercase() == "test" && request.password == "1234") {
            MockStateContainer.mock().user.loggedIn = true
        } else if (request.username.lowercase() == "reset") {
            MockStateContainer.reset()
        }
        call.respond(Response(MockStateContainer.mock().user.loggedIn, ""))
    }
    get("/logout") {
        MockStateContainer.mock().user.loggedIn = false
        call.respond(Response(MockStateContainer.mock().user.loggedIn.not(), ""))
    }
    route("/user") {
        get {
            preCheck(call, MockStateContainer)
            val user = MockStateContainer.mock().user
            call.respond(
                UserResponse(
                    "%.2f".format(MockStateContainer.mock().balance),
                    user.hourRate,
                    user.regimeStart,
                    user.regimeEnd,
                    user.regime
                )
            )
        }
        get("/balance") {
            preCheck(call, MockStateContainer)
            call.respond(BalanceResponse("%.2f".format(MockStateContainer.mock().balance)))
        }
        get("/regime/{date}") {
            preCheck(call, MockStateContainer)
            val date = ensureData(call.parameters["date"], "date")
            val (start, end) = MockStateContainer.mock().user.regimeForDate(DateUtil.date.parse(date))
            call.respond(RegimeResponse(start, end))
        }
    }
    route("/parking") {
        get {
            preCheck(call, MockStateContainer)
            call.respond(ParkingResponse(MockStateContainer.mock().active, MockStateContainer.mock().scheduled))
        }
        post {
            val request = call.receive<AddParkingRequest>()
            preCheck(call, MockStateContainer)

            MockStateContainer.mock().startParking(request)
            call.respond(Response(true, ""))
        }
        delete("/{id}") {
            val id = ensureData(call.parameters["id"]?.toLong(), "parking id")
            preCheck(call, MockStateContainer)

            MockStateContainer.mock().stopParking(id)
            call.respond(Response(true, ""))
        }
        get("/history") {
            preCheck(call, MockStateContainer)
            call.respond(HistoryResponse(MockStateContainer.mock().history))
        }
    }
    route("/visitor") {
        get {
            preCheck(call, MockStateContainer)
            call.respond(VisitorResponse(MockStateContainer.mock().visitors))
        }
        post {
            val request = call.receive<AddVisitorRequest>()
            preCheck(call, MockStateContainer)
            MockStateContainer.mock().addVisitor(request.name, request.license)
            call.respond(Response(true, ""))
        }
        delete("/{id}") {
            val id = ensureData(call.parameters["id"]?.toInt(), "visitor id")
            preCheck(call, MockStateContainer)

            MockStateContainer.mock().deleteVisitor(id)
            call.respond(Response(true, ""))
        }
    }
    route("/payment") {
        get {
            preCheck(call, MockStateContainer)
            call.respond(
                IdealResponse(
                    listOf("5,00", "10,00", "15,00", "20,00", "30,00", "40,00", "50,00", "100,00"),
                    listOf(
                        Issuer("SUCCESS", "Success"),
                        Issuer("PENDING", "Pending"),
                        Issuer("PENDING10", "Pending 10s"),
                        Issuer("ERROR", "Error"))
                )
            )
        }
        post {
            val request = call.receive<PaymentRequest>()
            preCheck(call, MockStateContainer)

            call.respond(MockStateContainer.mock().startPayment(request))
        }
        post("/complete") {
            val request = call.receive<CompleteRequest>()
            preCheck(call, MockStateContainer)
            val status = MockStateContainer.mock().checkPayment(request.transactionId)
            call.respond(Response(status.status == "success", ""))
        }
        get("/{id}") {
            val id = ensureData(call.parameters["id"], "payment id")
            preCheck(call, MockStateContainer)
            call.respond(MockStateContainer.mock().checkPayment(id))
        }
    }

}

fun preCheck(call: ApplicationCall, MockStateContainer: MockStateContainer) {
    if (MockStateContainer.mock().user.loggedIn.not()) {
        call.response.status(HttpStatusCode.Unauthorized)
        throw NoPermissionException()
    }
}

class MockRouteSelector(val builds: String?) : RouteSelector() {

    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        val mockHeader = context.call.request.headers["X-ParkeerAssistent-Mock"]
        if ("true".equals(mockHeader, ignoreCase = true)) {
            return RouteSelectorEvaluation.Constant
        }
        if (builds != null) {
            val mockBuilds = builds.split(",")
            val buildHeader = context.call.request.headers["X-ParkeerAssistent-Build"]
            if (mockBuilds.contains(buildHeader)) {
                return RouteSelectorEvaluation.Constant
            }
        }
        return RouteSelectorEvaluation.Failed
    }

}