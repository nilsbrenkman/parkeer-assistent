package nl.parkeerassistent.service

import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import nl.parkeerassistent.*
import nl.parkeerassistent.external.AddParkingSession
import nl.parkeerassistent.external.BooleanResponse
import nl.parkeerassistent.external.GetParkingSessions
import nl.parkeerassistent.external.StopParkingSession
import nl.parkeerassistent.model.AddParkingRequest
import nl.parkeerassistent.model.Parking
import nl.parkeerassistent.model.ParkingResponse
import nl.parkeerassistent.model.Response
import java.util.*

object ParkingService {

    enum class Method: Monitoring.Method {
        Get,
        Start,
        Stop,
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

        Monitoring.info(Method.Get, "SUCCESS")
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
        val match = regex.find(utc)!!
        val unix = match.groupValues[1].toLong()
        val date = Date(unix)
        return DateUtil.dateTime.format(date)
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
        return ServiceUtil.convertResponse(Method.Start, result)
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
        return ServiceUtil.convertResponse(Method.Stop, result)
    }

}
