package nl.parkeerassistent

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

}