package nl.parkeerassistent.service

import io.ktor.application.ApplicationCall
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.DateUtil
import nl.parkeerassistent.monitoring.Monitoring
import nl.parkeerassistent.Session
import nl.parkeerassistent.external.AddParkingSession
import nl.parkeerassistent.external.BooleanResponse
import nl.parkeerassistent.external.GetParkingSessions
import nl.parkeerassistent.external.StopParkingSession
import nl.parkeerassistent.model.AddParkingRequest
import nl.parkeerassistent.model.History
import nl.parkeerassistent.model.HistoryResponse
import nl.parkeerassistent.model.Parking
import nl.parkeerassistent.model.ParkingResponse
import nl.parkeerassistent.model.Response
import java.util.Calendar
import java.util.Date

object ParkingService {

    enum class Method: Monitoring.Method {
        Get,
        Start,
        Stop,
        History,
        ;
        override fun service(): Monitoring.Service {
            return Monitoring.Service.Parking
        }
        override fun method(): String {
            return name
        }
    }

    suspend fun get(call: ApplicationCall): ParkingResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val customerId = call.request.cookies["customerid"]!!

        val active = getParkingSessions(session, customerId, "Customer/Dashboard/GetActiveParkingSessions")
        val scheduled = getParkingSessions(session, customerId, "Customer/Dashboard/GetReservedParkingSessions")

        Monitoring.info(call, Method.Get, "SUCCESS")
        return ParkingResponse(active, scheduled)
    }

    private suspend fun getParkingSessions(session: Session, customerId: String, url: String): List<Parking> {
        val result = ApiHelper.client.post<GetParkingSessions>(ApiHelper.getUrl(url)) {
            ApiHelper.addHeaders(this, session)
            body = FormDataContent(formData = Parameters.build {
                append("cstId", customerId)
                append("length", "100")
            })
        }
        return result.data.map {
            Parking(it.Id, it.LP, convertTime(it.TimeStartUtc), convertTime(it.TimeEndUtc), it.CostMoney)
        }
    }

    private val regex = "/Date\\(([0-9]+)\\)/".toRegex()

    private fun convertTime(utc: String): String {
        val date = Date(getUnixTime(utc))
        return DateUtil.dateTime.format(date)
    }

    private fun getUnixTime(utc: String): Long {
        val match = regex.find(utc)!!
        return match.groupValues[1].toLong()
    }

    suspend fun start(call: ApplicationCall, request: AddParkingRequest): Response {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val calendar = Calendar.getInstance()
        calendar.time = request.start?.let { start -> DateUtil.dateTime.parse(start) } ?: run { Date() }
        calendar.add(Calendar.SECOND, 1)
        val start = calendar.time
        calendar.add(Calendar.MINUTE, request.timeMinutes)
        var end = calendar.time

        val regimeEnd = DateUtil.dateTime.parse(request.regimeTimeEnd)
        if (end.after(regimeEnd)) {
            end = regimeEnd
        }

        val requestBody = AddParkingSession(
            request.visitor.permitId,
            request.visitor.full(),
            DateUtil.dateTime.format(start),
            DateUtil.dateTime.format(end),
            DateUtil.dateTime.format(regimeEnd),
            true,
            true
        )

        val result = ApiHelper.client.post<BooleanResponse>(ApiHelper.getUrl("Customer/Dashboard/AddParkingSession")) {
            ApiHelper.addHeaders(this, session)
            contentType(ContentType.Application.Json)
            body = requestBody
        }
        return ServiceUtil.convertResponse(call, Method.Start, result)
    }

    suspend fun stop(call: ApplicationCall): Response {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val parkingId = call.parameters["id"]!!

        val result = ApiHelper.client.post<BooleanResponse>(ApiHelper.getUrl("Customer/Dashboard/StopParkingSession")) {
            ApiHelper.addHeaders(this, session)
            contentType(ContentType.Application.Json)
            body = StopParkingSession(parkingId.toInt())
        }
        return ServiceUtil.convertResponse(call, Method.Stop, result)
    }

    private val historyStart = DateUtil.dateTime.format(Date(0))

    suspend fun history(call: ApplicationCall): HistoryResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val customerId = call.request.cookies["customerid"]!!

        val result = ApiHelper.client.post<GetParkingSessions>(ApiHelper.getUrl("Customer/ParkingSession/GetParkingSessions")) {
            ApiHelper.addHeaders(this, session)
            body = FormDataContent(formData = Parameters.build {
                append("cstId", customerId)
                append("start", "0")
                append("length", "9999")
                append("timeStartUtc", historyStart)
                append("timeEndUtc", DateUtil.dateTime.format(Date()))
            })
        }
        val history = result.data.sortedByDescending { getUnixTime(it.TimeStartUtc) }
                                 .map { History(it.Id, it.LP, it.LPName, convertTime(it.TimeStartUtc), convertTime(it.TimeEndUtc), it.CostMoney) }

        return HistoryResponse(history)
    }


}
