package nl.parkeerassistent.service

import io.ktor.client.features.RedirectResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.Log
import nl.parkeerassistent.Session
import nl.parkeerassistent.ensureData
import nl.parkeerassistent.external.Balance
import nl.parkeerassistent.external.Order
import nl.parkeerassistent.external.Payment
import nl.parkeerassistent.external.PaymentOrder
import nl.parkeerassistent.external.Redirect
import nl.parkeerassistent.model.CompleteRequest
import nl.parkeerassistent.model.IdealResponse
import nl.parkeerassistent.model.Issuer
import nl.parkeerassistent.model.PaymentRequest
import nl.parkeerassistent.model.PaymentResponse
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.model.StatusResponse
import nl.parkeerassistent.monitoring.Monitoring
import org.jsoup.Connection
import org.jsoup.Jsoup

object PaymentService {

    enum class Method : Monitoring.Method {
        Ideal,
        Payment,
        Complete,
        Status,
        ;

        override fun service(): Monitoring.Service {
            return Monitoring.Service.Payment
        }

        override fun method(): String {
            return name
        }
    }

    val amounts = arrayListOf("2.50", "5.00", "10.00", "15.00", "20.00", "30.00", "40.00", "50.00", "100.00")

    fun ideal(session: Session): IdealResponse {

        val issuers = ArrayList<Issuer>()
        issuers.addAll(IdealBanks.values().map { i -> Issuer(i.name, i.displayName) })

        Monitoring.info(session.call, Method.Ideal, "SUCCESS")
        return IdealResponse(amounts, issuers)
    }

    suspend fun payment(session: Session, request: PaymentRequest): PaymentResponse {

        val payment = Payment(
            Balance(request.amount.toDouble(), "EUR"),
            Redirect("https://parkeerassistent.nl/completePayment")
        )

        val order = ApiHelper.client.post<PaymentOrder>(ApiHelper.getCloudUrl("v1/orders")) {
            ApiHelper.addCloudHeaders(this, session)
            body = payment
        }

        Log.json("order", order)

        val rabo = Jsoup.connect(order.redirectUrl).method(Connection.Method.GET).execute().parse()
        val issuer = rabo.select("a#issuer-" + request.issuerId)
        if (issuer.size != 1) {
            Monitoring.warn(session.call, Method.Payment, "ISSUER_NOT_FOUND")
            return PaymentResponse(order.redirectUrl, order.frontendId.toString())
        }
        val link = issuer[0].attr("href")

        return PaymentResponse(link, order.frontendId.toString())
    }

    suspend fun complete(session: Session, request: CompleteRequest): Response {
        try {
            val order = session.client.get<String>("https://aanmeldenparkeren.amsterdam.nl/api/orders?transactionType=topUpBalance&${request.data}")
            Log.debug(order)
        } catch(e: RedirectResponseException) {
            if (e.response.status.value == 302) {
                if (e.response.headers["Location"]?.startsWith("/top-up-balance/success") == true) {
                    if (ApiHelper.waitForOrder(session, request.transactionId.toLong())) {
                        Monitoring.info(session.call, Method.Complete, "SUCCESS")
                        return Response(true)
                    }
                    Monitoring.info(session.call, Method.Complete, "PENDING")
                    return Response(false)
                }
                Monitoring.info(session.call, Method.Complete, "FAILED")
                return Response(false)
            }
        }
        Monitoring.info(session.call, Method.Complete, "ERROR")
        return Response(false)
    }

    suspend fun status(session: Session): StatusResponse {
        val transactionId = ensureData(session.call.parameters["transactionId"], "transaction id")

        val order = ApiHelper.client.get<Order>(ApiHelper.getCloudUrl("v1/orders/$transactionId")) {
            ApiHelper.addCloudHeaders(this, session)
        }

        Log.json("order", order)

        when(order.orderStatus) {
            "Completed" -> {
                Monitoring.info(session.call, Method.Status, "SUCCESS")
                return StatusResponse("success")
            }
            "Processing" -> {
                Monitoring.info(session.call, Method.Status, "PENDING")
                return StatusResponse("pending")
            }
            else -> {
                Monitoring.info(session.call, Method.Status, "UNKNOWN")
                return StatusResponse("unknown")
            }
        }

    }

}

enum class IdealBanks(val displayName: String) {
    ABNANL2A("ABN AMRO"),
    ASNBNL21("ASN Bank"),
    BUNQNL2A("bunq"),
    HANDNL2A("Handelsbanken"),
    INGBNL2A("ING"),
    KNABNL2H("Knab"),
    RABONL2U("Rabobank"),
    RBRBNL21("RegioBank"),
    SNSBNL2A("SNS"),
    TRIONL2U("Triodos Bank"),
    FVLBNL22("Van Lanschot"),
}