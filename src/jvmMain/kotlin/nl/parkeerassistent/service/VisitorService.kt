package nl.parkeerassistent.service

import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.Monitoring
import nl.parkeerassistent.Session
import nl.parkeerassistent.external.AddNewLP
import nl.parkeerassistent.external.BooleanResponse
import nl.parkeerassistent.external.DeleteLP
import nl.parkeerassistent.external.GetLPs
import nl.parkeerassistent.model.AddVisitorRequest
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.model.Visitor
import nl.parkeerassistent.model.VisitorResponse

object VisitorService {

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

    suspend fun get(call: ApplicationCall): VisitorResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val customerId = call.request.cookies["customerid"]!!
        val permitId = call.request.cookies["permitid"]!!

        val result = ApiHelper.client.post<GetLPs>(ApiHelper.getUrl("LicensePlate/GetLPs")) {
            ApiHelper.addHeaders(this, session)
            body = FormDataContent(formData = Parameters.build {
                append("cstId", customerId)
                append("permitId", permitId)
                append("length", "100")
            })
        }
        Monitoring.info(Method.Get, "SUCCESS")
        return VisitorResponse(result.data.map{ Visitor(it.LPId,it.PermitId, it.LP, it.FormattedLP, it.Comment) })
    }

    suspend fun add(call: ApplicationCall, request: AddVisitorRequest): Response {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val customerId = call.request.cookies["customerid"]!!
        val permitId = call.request.cookies["permitid"]!!

        val result = ApiHelper.client.post<BooleanResponse>(ApiHelper.getUrl("Customer/LicensePlate/AddNewLP")) {
            ApiHelper.addHeaders(this, session)
            contentType(ContentType.Application.Json)
            body = AddNewLP(
                customerId.toInt(),
                permitId.toInt(),
                request.license,
                request.name)
        }
        return ServiceUtil.convertResponse(Method.Add, result)
    }

    suspend fun delete(call: ApplicationCall): Response {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val id = call.parameters["visitorId"]!!

        val result = ApiHelper.client.post<BooleanResponse>(ApiHelper.getUrl("Customer/LicensePlate/DeleteLP")) {
            ApiHelper.addHeaders(this, session)
            contentType(ContentType.Application.Json)
            body = DeleteLP(id.toInt())
        }
        return ServiceUtil.convertResponse(Method.Delete, result)
    }

}