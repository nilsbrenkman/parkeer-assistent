package nl.parkeerassistent.android.data

import nl.parkeerassistent.android.util.DateUtil.DateFormat.YearMonth
import java.util.*

data class HistoryGroup(
    val date: Date
) : Comparable<HistoryGroup> {

    override fun compareTo(other: HistoryGroup): Int {
        if (this == other) {
            return 0
        }
        return this.date.compareTo(other.date)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is HistoryGroup) {
            return YearMonth.format(this.date) == YearMonth.format(other.date)
        }
        return false
    }

    override fun hashCode(): Int {
        return YearMonth.format(this.date).hashCode()
    }

}
