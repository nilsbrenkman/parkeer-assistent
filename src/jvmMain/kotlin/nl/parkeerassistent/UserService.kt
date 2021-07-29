package nl.parkeerassistent

import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import nl.parkeerassistent.external.GetBalanceInfo
import nl.parkeerassistent.external.GetPermitsByCustomer
import nl.parkeerassistent.model.BalanceResponse
import nl.parkeerassistent.model.UserResponse

object UserService {

    enum class Method: Monitoring.Method {
        Get,
        Balance,
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

        val info = getInfo(session, customerId, permitId.toString())

        Monitoring.info(Method.Get, "SUCCESS")
        return UserResponse(balance, info.hourRate, info.regimeEndTime)
    }

    suspend fun balance(call: ApplicationCall): BalanceResponse {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val balance = getBalance(session)

        Monitoring.info(Method.Balance, "SUCCESS")
        return BalanceResponse(balance)
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

    suspend fun getInfo(session: Session, customerId: String, permitId: String): GetBalanceInfo {
        val params = Parameters.build {
            append("customerId", customerId)
            append("permitId", permitId)
        }.formUrlEncode()
        return ApiHelper.client.get(ApiHelper.getUrl("Customer/Dashboard/GetBalanceInfo?$params")) {
            ApiHelper.addHeaders(this, session)
        }
    }

}