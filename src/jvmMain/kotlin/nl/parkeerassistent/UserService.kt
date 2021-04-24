package nl.parkeerassistent

import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.coroutines.runBlocking
import nl.parkeerassistent.external.GetBalanceInfo
import nl.parkeerassistent.external.GetPermitsByCustomer
import nl.parkeerassistent.model.BalanceResponse
import nl.parkeerassistent.model.UserResponse

object UserService {

    suspend fun get(call: ApplicationCall) {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        val customerId = call.request.cookies["customerid"]!!

        try {
            val balance = getBalance(session)
            val permitId = getPermitId(session, customerId)
            val permitIdCookie = Cookie("permitid", permitId.toString())
            call.response.cookies.append(permitIdCookie)

            val info = getInfo(session, customerId, permitId.toString())

            val response = UserResponse(balance, info.hourRate, info.regimeEndTime)
            call.respond(response)
            return
        } catch (e: RedirectResponseException) {
            //
        }
        call.response.status(HttpStatusCode.Forbidden)
    }

    fun balance(call: ApplicationCall) {
        val sessionCookie = call.request.cookies["session"]!!
        val session = Session(sessionCookie)

        try {
            runBlocking {
                val balance = getBalance(session)
                call.respond(BalanceResponse(balance))
            }
            return
        } catch (e: RedirectResponseException) {
            //
        }
        call.response.status(HttpStatusCode.Forbidden)
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