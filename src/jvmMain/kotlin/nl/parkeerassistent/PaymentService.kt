package nl.parkeerassistent

import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.http.*
import nl.parkeerassistent.external.RequestPaymentRequest
import nl.parkeerassistent.external.RequestPaymentResponse
import nl.parkeerassistent.model.*
import org.jsoup.Connection
import org.jsoup.Jsoup

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

        val ideal = session.send(Jsoup.connect(ApiHelper.baseUrl + "Customer/Payment/IdealPayment").method(Connection.Method.GET))

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

        val issuerSelect = ideal.select("select[name=\"IssuerId\"]")
        if (issuerSelect.size != 1) {
            throw ServiceException(ServiceException.Type.SCREENSCRAPING, "Unable to find issuer options")
        }
        val issuers = ArrayList<Issuer>()
        for (option in issuerSelect[0].select("option")) {
            val issuerId = option.attr("value")
            val name = option.text()
            issuers.add(Issuer(issuerId, name))
        }

        Monitoring.info(Method.Ideal, "SUCCESS")
        return IdealResponse(amounts, issuers)
    }

    suspend fun payment(call: ApplicationCall, request: PaymentRequest): PaymentResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val requestBody = RequestPaymentRequest(request.amount, request.issuerId)

        val result = ApiHelper.client.post<RequestPaymentResponse>(ApiHelper.getUrl("Customer/Payment/RequestPayment")) {
            ApiHelper.addHeaders(this, session, referer = "Customer/Payment/IdealPayment")
            contentType(ContentType.Application.Json)
            body = requestBody
        }
        Monitoring.info(Method.Payment, "SUCCESS")
        return PaymentResponse(result.issuerAuthenticationUrl, result.transactionId)
    }

    fun status(call: ApplicationCall): StatusResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val transactionId = call.parameters["transactionId"]!!

        val status = session.send(Jsoup.connect(ApiHelper.baseUrl + "Customer/Payment/PaymentStatus?trxid=" + transactionId).method(Connection.Method.GET))

        if ("Betalingsstatus - Parkeerapplicatie" != status.title()) {
            throw ServiceException(ServiceException.Type.SCREENSCRAPING, "Unable to load status page")
        }

        var result = "unknown"
        val transactionIdInfo = status.select("span.transactionIdInfo")

        outer@ for (info in transactionIdInfo) {
            for (sibling in info.parent().children()) {
                if (sibling.text().contains("Opwaarderen van het geldsaldo is nog bezig")) {
                    result = "pending"
                    break@outer
                }
                if (sibling.text().contains("Opwaarderen van het geldsaldo is gelukt")) {
                    result = "success"
                    break@outer
                }
            }
        }

        Monitoring.info(Method.Status, "SUCCESS")
        return StatusResponse(result)
    }

}