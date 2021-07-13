package nl.parkeerassistent

import kotlinx.datetime.internal.JSJoda.DateTimeFormatter
import kotlinx.datetime.internal.JSJoda.LocalDate
import kotlinx.datetime.internal.JSJoda.LocalDateTime
import nl.parkeerassistent.model.Parking
import kotlin.js.Date

object Util {

    fun formatAmount(cost: Double): String {
        var costString = "$cost"
        val sep = costString.indexOf('.')
        if (sep == -1) {
            return "${costString}.00"
        }
        costString = "${cost}00"
        return costString.substring(0, sep + 3)
    }

    fun formatTime(date: Date): String {
        val min = "0${date.getMinutes()}"
        return "${date.getHours()}:${min.substring(min.length - 2)}"
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
    val timeRangeFull = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    val timeRangePartial = DateTimeFormatter.ofPattern("HH:mm")

    fun getTimeRange(parking: Parking): String {
        val startDate = LocalDateTime.parse(parking.startTime, formatter)
        val endDate = LocalDateTime.parse(parking.endTime, formatter)
        if (startDate.year() == endDate.year() && startDate.dayOfYear() == endDate.dayOfYear()) {
            return startDate.format(timeRangeFull) + " - " + endDate.format(timeRangePartial)
        }
        return startDate.format(timeRangeFull) + " - " + endDate.format(timeRangeFull)
    }

}