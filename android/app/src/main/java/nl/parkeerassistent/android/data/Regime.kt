package nl.parkeerassistent.android.data

import nl.parkeerassistent.android.util.DateUtil
import java.util.*

data class Regime(
    val start: Date,
    val end: Date
) {
    constructor(regimeTimeStart: String, regimeTimeEnd: String) : this(
        DateUtil.parse(regimeTimeStart),
        DateUtil.parse(regimeTimeEnd)
    )
}
