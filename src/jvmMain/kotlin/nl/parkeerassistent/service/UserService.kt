package nl.parkeerassistent.service

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import nl.parkeerassistent.ApiHelper
import nl.parkeerassistent.DateUtil
import nl.parkeerassistent.Session
import nl.parkeerassistent.ensureData
import nl.parkeerassistent.external.Permit
import nl.parkeerassistent.external.Permits
import nl.parkeerassistent.model.BalanceResponse
import nl.parkeerassistent.model.Regime
import nl.parkeerassistent.model.RegimeDay
import nl.parkeerassistent.model.RegimeResponse
import nl.parkeerassistent.model.UserResponse
import nl.parkeerassistent.monitoring.Monitoring
import java.time.DayOfWeek
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

    suspend fun get(session: Session): UserResponse {

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
        val fullRegime = getFullRegime(permit)

        Monitoring.info(session.call, Method.Get, "SUCCESS")
        return UserResponse(formatBalance(permits), permit.parkingRate.value, regime.regimeTimeStart, regime.regimeTimeEnd, fullRegime)
    }

    suspend fun balance(session: Session): BalanceResponse {
        val permits = getPermits(session)

        Monitoring.info(session.call, Method.Balance, "SUCCESS")
        return BalanceResponse(formatBalance(permits))
    }

    private fun formatBalance(permits: Permits) = "%.2f".format(permits.wallet.balance)

    suspend fun regime(session: Session): RegimeResponse {
        val regimeDate = ensureData(session.call.parameters["date"], "date")

        val permits = getPermits(session)

        val regime = getRegime(permits.permits.first(), DateUtil.date.parse(regimeDate).toInstant())

        Monitoring.info(session.call, Method.Regime, "SUCCESS")
        return regime
    }

    suspend fun getPermits(session: Session): Permits {
        return ApiHelper.client.get(ApiHelper.getCloudUrl("v1/permits")) {
            ApiHelper.addCloudHeaders(this, session)
            parameter("status", "Actief")
        }
    }

    fun getRegime(permit: Permit, date: Instant): RegimeResponse {
        val calendar = Calendar.getInstance()
        calendar.time = Date.from(date)
        val dayOfWeek = DayOfWeek.values().get(calendar.get(Calendar.DAY_OF_WEEK) - 1)
        val day = permit.paymentZones.first().days.first{ it.dayOfWeek == dayOfWeek.alias }
        val startTime = DateUtil.dateWithTime(calendar.time, day.startTime)
        val endTime = DateUtil.dateWithTime(calendar.time, day.endTime)
        return RegimeResponse(startTime, endTime)
    }

    fun getFullRegime(permit: Permit): Regime {
        val days = permit.paymentZones.first().days
            .filter { d -> ! ("00:00" == d.startTime && "24:00" == d.endTime) }
            .mapNotNull { d ->
                val dayOfWeek = DayOfWeek.fromAlias(d.dayOfWeek)
                dayOfWeek?.let { RegimeDay(it.name, d.startTime, d.endTime) }
            }
        return Regime(days)
    }

    enum class DayOfWeek(val alias: String) {
        SUN("Zondag"),
        MON("Maandag"),
        TUE("Dinsdag"),
        WED("Woensdag"),
        THU("Donderdag"),
        FRI("Vrijdag"),
        SAT("Zaterdag"),
        ;
        companion object {
            fun fromAlias(a: String): DayOfWeek? {
                for (d in DayOfWeek.values()) {
                    if (d.alias == a) {
                        return d
                    }
                }
                return null
            }
        }
    }

}