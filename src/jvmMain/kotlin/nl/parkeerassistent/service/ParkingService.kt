package nl.parkeerassistent.service

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.DateUtil
import nl.parkeerassistent.Session
import nl.parkeerassistent.ensureData
import nl.parkeerassistent.external.AddParking
import nl.parkeerassistent.external.AddParkingSession
import nl.parkeerassistent.external.ParkingOrder
import nl.parkeerassistent.external.ParkingSession
import nl.parkeerassistent.external.ParkingSessions
import nl.parkeerassistent.external.StopParking
import nl.parkeerassistent.external.StopParkingSession
import nl.parkeerassistent.json
import nl.parkeerassistent.model.AddParkingRequest
import nl.parkeerassistent.model.HistoryResponse
import nl.parkeerassistent.model.Parking
import nl.parkeerassistent.model.ParkingResponse
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.monitoring.Monitoring
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date

object ParkingService {

    private val log = LoggerFactory.getLogger(ParkingService::class.java)

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

    suspend fun get(session: Session): ParkingResponse {

        val active = getParkingSessions(session, ParkingSessionType.Active)
        val scheduled = getParkingSessions(session, ParkingSessionType.Scheduled)

        Monitoring.info(session.call, Method.Get, "SUCCESS")
        return ParkingResponse(active, scheduled)
    }

    private suspend fun getParkingSessions(session: Session, type: ParkingSessionType): List<Parking> {

        val result = parkingSessions(session, type)

        val parkingSessions = if (type != ParkingSessionType.Completed) {
            result.sortedBy { ZonedDateTime.parse(it.startDate, DateUtil.gmtDateFormatter) }
        } else {
            result.sortedByDescending { ZonedDateTime.parse(it.startDate, DateUtil.gmtDateFormatter) }
        }

        return parkingSessions.map {
            Parking(it.psRightId, it.vehicleId, it.visitorName, convertTime(it.startDate), convertTime(it.endDate), it.parkingCost.value)
        }
    }

    enum class ParkingSessionType(val status: String) {
        Active("Actief"),
        Scheduled("Toekomstig"),
        Completed("Voltooid")
    }

    private fun convertTime(gmt: String): String {
        val date = DateUtil.gmtDateFormatter.parse(gmt)
        return DateUtil.dateFormatter.format(date)
    }

    suspend fun start(session: Session, request: AddParkingRequest): Response {
        val reportCode = ensureData(session.permit?.reportCode, "report code")
        val paymentZoneId = ensureData(session.permit?.paymentZoneId, "payment zone id")

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

        val parkingSession = AddParking(
            AddParkingSession(
                reportCode,
                paymentZoneId,
                request.visitor.license,
                DateUtil.gmtDateFormatter.format(start.toInstant()),
                DateUtil.gmtDateFormatter.format(end.toInstant())
            )
        )

        log.json("parkingSession", parkingSession)

        val result = ApiHelper.client.post<ParkingOrder>(ApiHelper.getCloudUrl("v1/orders")) {
            ApiHelper.addCloudHeaders(this, session)
            body = parkingSession
        }

        log.json("result", result)

        val completed = ApiHelper.waitForOrder(session, result.frontendId)

        return ServiceUtil.convertResponse(session.call, Method.Start, completed)
    }

    suspend fun stop(session: Session): Response {
        val parkingId = ensureData(session.call.parameters["id"]?.toLong(), "parking id")
        val reportCode = ensureData(session.permit?.reportCode, "report code")

        val original = findParkingSession(session, parkingId) ?: throw ServiceException(ServiceException.Type.MISSING_DATA, "Session not found)")

        val parkingSession = StopParking(
            StopParkingSession(
                reportCode,
                parkingId,
                original.startDate,
                DateUtil.gmtDateFormatter.format(Instant.now())
            )
        )

        log.json("parkingSession", parkingSession)

        val result = ApiHelper.client.post<ParkingOrder>(ApiHelper.getCloudUrl("v1/orders")) {
            ApiHelper.addCloudHeaders(this, session)
            body = parkingSession
        }

        log.json("result", result)

        val completed = ApiHelper.waitForOrder(session, result.frontendId)

        return ServiceUtil.convertResponse(session.call, Method.Stop, completed)
    }

    private suspend fun findParkingSession(session: Session, parkingId: Long): ParkingSession? {
        val parkingSession = parkingSessions(session, ParkingSessionType.Active)
            .find { p -> (p.psRightId == parkingId) }
        return parkingSession ?: parkingSessions(session, ParkingSessionType.Scheduled)
            .find { p -> (p.psRightId == parkingId) }
    }

    private suspend fun parkingSessions(session: Session, type: ParkingSessionType): List<ParkingSession> {
        val result = ApiHelper.client.get<ParkingSessions>(ApiHelper.getCloudUrl("v2/parkingsessions")) {
            ApiHelper.addCloudHeaders(this, session)
            parameter("status", type.status)
            parameter("itemsPerPage", 100)
            parameter("page", 1)
        }
        return result.parkingSession.filterNot {p -> p.isCancelled && p.parkingCost.value == 0.0}
    }

    suspend fun history(session: Session): HistoryResponse {
        val history = getParkingSessions(session, ParkingSessionType.Completed)

        return HistoryResponse(history)
    }

}
