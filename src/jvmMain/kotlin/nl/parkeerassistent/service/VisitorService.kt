package nl.parkeerassistent.service

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.License
import nl.parkeerassistent.MigrationUtil
import nl.parkeerassistent.Session
import nl.parkeerassistent.ensureData
import nl.parkeerassistent.external.LicensePlate
import nl.parkeerassistent.json
import nl.parkeerassistent.model.AddVisitorRequest
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.model.Visitor
import nl.parkeerassistent.model.VisitorResponse
import nl.parkeerassistent.monitoring.Monitoring
import org.slf4j.LoggerFactory

object VisitorService {

    private val log = LoggerFactory.getLogger(VisitorService::class.java)

    enum class Method: Monitoring.Method {
        Get,
        Add,
        Delete,
        ;
        override fun service(): Monitoring.Service {
            return Monitoring.Service.Visitor
        }
        override fun method(): String {
            return name
        }
    }

    suspend fun get(session: Session): VisitorResponse {
        val reportCode = ensureData(session.permit?.reportCode, "report code")

        val licensePlates = ApiHelper.client.get<List<LicensePlate>>(ApiHelper.getCloudUrl("v1/licenseplates")) {
            ApiHelper.addCloudHeaders(this, session)
            parameter("reportCode", reportCode)
        }

        Monitoring.info(session.call, Method.Get, "SUCCESS")
        return VisitorResponse(licensePlates.map {
            Visitor(
                MigrationUtil.createId(it.vehicleId),
                reportCode,
                it.vehicleId,
                License.format(it.vehicleId),
                it.visitorName
            )
        })
    }

    suspend fun add(session: Session, request: AddVisitorRequest): Response {
        val licensePlate = LicensePlate(License.normalise(request.license), request.name, session.permit?.reportCode)
        val result = session.client.post<LicensePlate>(ApiHelper.getCloudUrl("v1/licenseplates")) {
            ApiHelper.addCloudHeaders(this, session)
            body = licensePlate
        }
        log.json("LicensePlate", result)
        return ServiceUtil.convertResponse(session.call, Method.Add, true)
    }

    suspend fun delete(session: Session): Response {
        val id = ensureData(session.call.parameters["visitorId"]?.toInt(),"visitor id")

        val visitors = get(session).visitors
        val visitor = ensureData(visitors.firstOrNull{ v -> v.visitorId == id }, "visitor found")

        val licensePlate = LicensePlate(visitor.license, visitor.name, session.permit?.reportCode)

        val result = ApiHelper.client.delete<String>(ApiHelper.getCloudUrl("v1/licenseplates")) {
            ApiHelper.addCloudHeaders(this, session)
            body = licensePlate
        }

        return ServiceUtil.convertResponse(session.call, Method.Delete, result == "OK")
    }

}