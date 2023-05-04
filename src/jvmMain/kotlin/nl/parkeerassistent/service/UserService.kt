package nl.parkeerassistent.service

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.CallSession
import nl.parkeerassistent.DateUtil
import nl.parkeerassistent.ensureData
import nl.parkeerassistent.external.Permit
import nl.parkeerassistent.external.Permits
import nl.parkeerassistent.model.BalanceResponse
import nl.parkeerassistent.model.RegimeResponse
import nl.parkeerassistent.model.UserResponse
import nl.parkeerassistent.monitoring.Monitoring
import java.time.Instant
import java.util.Calendar
import java.util.Date

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

    suspend fun get(session: CallSession): UserResponse {

        val permits = getPermits(session)

        if (permits.permits.size != 1) {
            Monitoring.warn(session.call, Method.Get, "PERMIT_NOT_ONE")
            throw ServiceException(ServiceException.Type.API, "Response contained wrong number of permits [permits=${permits.permits.size}]")
        }
        val permit = permits.permits.first()

        if (session.permit?.paymentZoneId == null) {
            session.permit = nl.parkeerassistent.Permit(permit.reportCode, permit.paymentZones.first().id)
        }

        val regime = getRegime(permit, Instant.now())

        Monitoring.info(session.call, Method.Get, "SUCCESS")
        return UserResponse(formatBalance(permits), permit.parkingRate.value, regime.regimeTimeStart, regime.regimeTimeEnd)
    }

    suspend fun balance(session: CallSession): BalanceResponse {
        val permits = getPermits(session)

        Monitoring.info(session.call, Method.Balance, "SUCCESS")
        return BalanceResponse(formatBalance(permits))
    }

    private fun formatBalance(permits: Permits) = "%.2f".format(permits.wallet.balance)

    suspend fun regime(session: CallSession): RegimeResponse {
        val regimeDate = ensureData(session.call.parameters["date"], "date")

        val permits = getPermits(session)

        val regime = getRegime(permits.permits.first(), DateUtil.date.parse(regimeDate).toInstant())

        Monitoring.info(session.call, Method.Regime, "SUCCESS")
        return regime
    }

    suspend fun getPermits(session: CallSession): Permits {
        return ApiHelper.client.get(ApiHelper.getCloudUrl("v1/permits")) {
            ApiHelper.addCloudHeaders(this, session)
            parameter("status", "Actief")
        }
    }

    fun getRegime(permit: Permit, date: Instant): RegimeResponse {
        val calendar = Calendar.getInstance()
        calendar.time = Date.from(date)
        val dayOfWeek = DayOfWeek.values().get(calendar.get(Calendar.DAY_OF_WEEK) - 1)
        val day = permit.paymentZones.first().days.first{ it.dayOfWeek == dayOfWeek.name }
        val startTime = getTime(calendar, day.startTime)
        val endTime = getTime(calendar, day.endTime)
        return RegimeResponse(startTime, endTime)
    }

    fun getTime(calendar: Calendar, time: String): String {
        val hour = time.substring(0, 2).toInt()
        val minutes = time.substring(3, 5).toInt()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return DateUtil.dateTime.format(calendar.time)
    }

    enum class DayOfWeek {
        Zondag, Maandag, Dinsdag, Woensdag, Donderdag, Vrijdag, Zaterdag
    }

}