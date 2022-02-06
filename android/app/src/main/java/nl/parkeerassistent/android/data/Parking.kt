package nl.parkeerassistent.android.data

import kotlinx.serialization.Serializable
import nl.parkeerassistent.android.util.DateUtil
import java.util.*

@Serializable
data class Parking(
    val id: Int,
    val license: String,
    val name: String? = null,
    val startTime: String,
    var endTime: String,
    var cost: Double
) {

    val startDate: Date
        get() { return DateUtil.parse(startTime) }

    val endDate: Date
        get() { return DateUtil.parse(endTime) }

}