package nl.parkeerassistent.service

import io.ktor.application.ApplicationCall
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.Parameters
import io.ktor.http.contentType
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.DateUtil
import nl.parkeerassistent.Session
import nl.parkeerassistent.external.CalculateBalanceRequest
import nl.parkeerassistent.external.CalculateBalanceResponse
import nl.parkeerassistent.external.GetPermitsByCustomer
import nl.parkeerassistent.model.BalanceResponse
import nl.parkeerassistent.model.RegimeResponse
import nl.parkeerassistent.model.UserResponse
import nl.parkeerassistent.monitoring.Monitoring
import java.util.Calendar
import java.util.Calendar.HOUR
import java.util.Calendar.MINUTE

object UserService {

    enum class Method : Monitoring.Method {
        Get,
        Balance,
        Regime,
        ;

        override fun service(): Monitoring.Service {
            return Monitoring.Service.User
        }

        override fun method(): String {
            return name
        }
    }

    suspend fun get(call: ApplicationCall): UserResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val customerId = call.request.cookies["customerid"]!!

        val balance = getBalance(session)
        val permitId = getPermitId(session, customerId)
        val permitIdCookie = Cookie("permitid", permitId.toString())
        call.response.cookies.append(permitIdCookie)

        val info = getInfo(session, permitId.toString())

        Monitoring.info(call, Method.Get, "SUCCESS")
        return UserResponse(balance, info.hourRate, info.regimeStartTime, info.regimeEndTime)
    }

    suspend fun balance(call: ApplicationCall): BalanceResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val balance = getBalance(session)

        Monitoring.info(call, Method.Balance, "SUCCESS")
        return BalanceResponse(balance)
    }

    suspend fun regime(call: ApplicationCall): RegimeResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val permitId = call.request.cookies["permitid"]!!
        val regimeDate = call.parameters["date"]!!
        val calendar = Calendar.getInstance()
        calendar.time = DateUtil.date.parse(regimeDate)
        calendar.timeZone = DateUtil.amsterdam
        calendar.set(HOUR, 0)
        calendar.set(MINUTE, 1)
        val start = calendar.time
        calendar.set(HOUR, 23)
        calendar.set(MINUTE, 59)
        val end = calendar.time

        val requestBody = CalculateBalanceRequest(
            permitId.toInt(),
            DateUtil.dateTime.format(start),
            DateUtil.dateTime.format(end)
        )

        val result = ApiHelper.client.post<CalculateBalanceResponse>(ApiHelper.getUrl("Customer/Dashboard/CalculateBalance")) {
            ApiHelper.addHeaders(this, session)
            contentType(ContentType.Application.Json)
            body = requestBody
        }

        Monitoring.info(call, Method.Regime, "SUCCESS")
        return RegimeResponse(result.regimeStartTime, result.regimeEndTime)
    }

    suspend fun getBalance(session: Session): String {
        val result = ApiHelper.client.get<String>(ApiHelper.getUrl("Customer/Dashboard/GetMoneyBalanceForCustomer")) {
            ApiHelper.addHeaders(this, session)
        }
        val dot = result.indexOf(".")
        return result.substring(0, dot + 3)
    }

    suspend fun getPermitId(session: Session, customerId: String): Int {
        val result =
            ApiHelper.client.post<GetPermitsByCustomer>(ApiHelper.getUrl("Customer/Permit/GetPermitsByCustomer")) {
                ApiHelper.addHeaders(this, session)
                body = FormDataContent(formData = Parameters.build {
                    append("cstId", customerId)
                    append("length", "100")
                })
            }
        return result.data[0].Id
    }

    suspend fun getInfo(session: Session, permitId: String): CalculateBalanceResponse {
        val calendar = Calendar.getInstance()
        calendar.timeZone = DateUtil.amsterdam
        calendar.set(HOUR, 0)
        calendar.set(MINUTE, 1)
        val start = calendar.time
        calendar.set(HOUR, 23)
        calendar.set(MINUTE, 59)
        val end = calendar.time

        val requestBody = CalculateBalanceRequest(
            permitId.toInt(),
            DateUtil.dateTime.format(start),
            DateUtil.dateTime.format(end)
        )

        return ApiHelper.client.post(ApiHelper.getUrl("Customer/Dashboard/CalculateBalance")) {
            ApiHelper.addHeaders(this, session)
            contentType(ContentType.Application.Json)
            body = requestBody
        }

    }

}