package nl.parkeerassistent.service

import io.ktor.application.ApplicationCall
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.DateUtil
import nl.parkeerassistent.Session
import nl.parkeerassistent.external.GetTransactionsHistoryByCustomer
import nl.parkeerassistent.external.RequestPaymentRequest
import nl.parkeerassistent.external.RequestPaymentResponse
import nl.parkeerassistent.model.IdealResponse
import nl.parkeerassistent.model.Issuer
import nl.parkeerassistent.model.PaymentRequest
import nl.parkeerassistent.model.PaymentResponse
import nl.parkeerassistent.model.StatusResponse
import nl.parkeerassistent.monitoring.Monitoring
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.time.Instant
import java.time.temporal.ChronoUnit

object PaymentService {

    enum class Method : Monitoring.Method {
        Ideal,
        Payment,
        Status,
        ;

        override fun service(): Monitoring.Service {
            return Monitoring.Service.Payment
        }

        override fun method(): String {
            return name
        }
    }

    fun ideal(call: ApplicationCall): IdealResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val ideal = session.send(Jsoup.connect(ApiHelper.baseUrl + "Customer/OmniKassaPayment/OmniKassaPayment").method(Connection.Method.GET))

        if ("Geldsaldo opwaarderen - Parkeerapplicatie" != ideal.title()) {
            throw ServiceException(ServiceException.Type.SCREENSCRAPING, "Unable to load ideal page")
        }

        val amountSelect = ideal.select("select[name=\"Amount\"]")
        if (amountSelect.size != 1) {
            throw ServiceException(ServiceException.Type.SCREENSCRAPING, "Unable to find amount options")
        }
        val amounts = ArrayList<String>()
        for (option in amountSelect[0].select("option")) {
            val amount = option.attr("value")
            amounts.add(amount)
        }

        val issuers = ArrayList<Issuer>()
        issuers.addAll(IdealBanks.values().map { i -> Issuer(i.name, i.displayName) })

        Monitoring.info(call, Method.Ideal, "SUCCESS")
        return IdealResponse(amounts, issuers)
    }

    suspend fun payment(call: ApplicationCall, request: PaymentRequest): PaymentResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val requestBody = RequestPaymentRequest(request.amount)

        val result = ApiHelper.client.post<RequestPaymentResponse>(ApiHelper.getUrl("Customer/OmniKassaPayment/RequestPayment")) {
            ApiHelper.addHeaders(this, session, referer = "Customer/OmniKassaPayment/OmniKassaPayment")
            contentType(ContentType.Application.Json)
            body = requestBody
        }

        val rabo = Jsoup.connect(result.issuerAuthenticationUrl).method(Connection.Method.GET).execute().parse()
        val issuer = rabo.select("a#issuer-" + request.issuerId)
        if (issuer.size != 1) {
            throw ServiceException(ServiceException.Type.SCREENSCRAPING, "Unable to find issuer")
        }
        val link = issuer[0].attr("href")

        val order = rabo.select("div.ordernumber")
        if (order.size != 1) {
            throw ServiceException(ServiceException.Type.SCREENSCRAPING, "Unable to find order")
        }
        val orderId = order[0].text().replace(Regex("\\D"), "")

        Monitoring.info(call, Method.Payment, "SUCCESS")
        return PaymentResponse(link, orderId)
    }

    suspend fun status(call: ApplicationCall): StatusResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val customerId = call.request.cookies["customerid"]!!
        val transactionId = call.parameters["transactionId"]!!

        val result = ApiHelper.client.post<GetTransactionsHistoryByCustomer>(ApiHelper.getUrl("Customer/Transaction/GetTransactionsHistoryByCustomer")) {
            ApiHelper.addHeaders(this, session)
            body = FormDataContent(formData = Parameters.build {
                append("cstId", customerId)
                append("start", "0")
                append("length", "9999")
                append("startDate", DateUtil.dateFormatter.format(Instant.now().minus(24, ChronoUnit.HOURS)))
                append("endDate", DateUtil.dateFormatter.format(Instant.now()))
            })
        }

        for (transaction in result.data) {
            if (transactionId == transaction.TransactionId) {
                if (transaction.Status == "success") {
                    Monitoring.info(call, Method.Status, "SUCCESS")
                    return StatusResponse("success")
                }
                if (transaction.Status == "cancelled") {
                    Monitoring.info(call, Method.Status, "CANCELLED")
                    return StatusResponse("error")
                }
                if (transaction.Status == "open") {
                    Monitoring.info(call, Method.Status, "PENDING")
                    return StatusResponse("pending")
                }
                Monitoring.info(call, Method.Status, "UNKNOWN")
                return StatusResponse("unknown")
            }
        }
        Monitoring.info(call, Method.Status, "NOT_FOUND")
        return StatusResponse("unknown")
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