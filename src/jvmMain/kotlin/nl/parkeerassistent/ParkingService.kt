package nl.parkeerassistent

import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.coroutines.runBlocking
import nl.parkeerassistent.external.AddParkingSession
import nl.parkeerassistent.external.BooleanResponse
import nl.parkeerassistent.external.GetParkingSessions
import nl.parkeerassistent.external.StopParkingSession
import nl.parkeerassistent.model.AddParkingRequest
import nl.parkeerassistent.model.Parking
import nl.parkeerassistent.model.ParkingResponse
import nl.parkeerassistent.model.Response
import java.text.SimpleDateFormat
import java.util.*

object ParkingService {

    fun get(call: ApplicationCall) {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val customerId = call.request.cookies["customerid"]!!

        try {
            runBlocking {
                val active = getParkingSessions(session, customerId, "Customer/Dashboard/GetActiveParkingSessions")
                val scheduled = getParkingSessions(session, customerId, "Customer/Dashboard/GetReservedParkingSessions")
                val response = ParkingResponse(active, scheduled)
                call.respond(response)
            }
            return
        } catch (e: RedirectResponseException) {
            //
        }
        call.response.status(HttpStatusCode.Forbidden)
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
    private val sdf = SimpleDateFormat("dd/MM HH:mm")

    private fun convertTime(utc: String): String {
        val match = regex.find(utc)!!
        val unix = match.groupValues[1].toLong()
        val date = Date(unix)
        return sdf.format(date)
    }

    private val dateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")

    fun start(request: AddParkingRequest, call: ApplicationCall) {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.SECOND, 1)
        val start = calendar.time
        calendar.add(Calendar.MINUTE, request.timeMinutes)
        var end = calendar.time

        val regimeEnd = dateTime.parse(request.regimeTimeEnd)
        if (end.after(regimeEnd)) {
            end = regimeEnd
        }

        val requestBody = AddParkingSession(
            request.visitor.permitId,
            request.visitor.full(),
            dateTime.format(start),
            dateTime.format(end),
            request.regimeTimeEnd,
            true,
            true
        )
        try {
            runBlocking {
                val result = ApiHelper.client.post<BooleanResponse>(ApiHelper.getUrl("Customer/Dashboard/AddParkingSession")) {
                    ApiHelper.addHeaders(this, session)
                    contentType(ContentType.Application.Json)
                    body = requestBody
                }
                call.respond(Response(result.successful))
            }
            return
        } catch (e: RedirectResponseException) {
            //
        }
        call.response.status(HttpStatusCode.Forbidden)
    }

    fun stop(call: ApplicationCall) {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val parkingId = call.parameters["id"]!!

        try {
            runBlocking {
                val result = ApiHelper.client.post<BooleanResponse>(ApiHelper.getUrl("Customer/Dashboard/StopParkingSession")) {
                    ApiHelper.addHeaders(this, session)
                    contentType(ContentType.Application.Json)
                    body = StopParkingSession(parkingId.toInt())
                }
                call.respond(Response(result.successful))
            }
            return
        } catch (e: RedirectResponseException) {
            //
        }
        call.response.status(HttpStatusCode.Forbidden)
    }

}
