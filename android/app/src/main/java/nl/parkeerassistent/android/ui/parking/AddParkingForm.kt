package nl.parkeerassistent.android.ui.parking

import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.Regime
import nl.parkeerassistent.android.util.DateUtil
import java.util.*
import java.util.concurrent.TimeUnit

data class AddParkingForm(
    val balance: Double,
    var regime: Regime,
    val hourRate: Double
) {

    var start: Date = Date()
        set(value) {
            if (fixedEnd) {
                val diff = end.time - value.time
                minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
            }
            field = value
        }
    var minutes = 0
    var end: Date
        get() {
            val c = Calendar.getInstance(DateUtil.amsterdam)
            c.time = start
            c.add(Calendar.MINUTE, minutes)
            return c.time
        }
        set(value) {
            val diff = value.time - start.time
            val rounding = if (fixedEnd) 1 else 0
            minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt() + rounding
        }
    val cost: Double
        get() = (minutes.toDouble() / 60.0) * hourRate


    var fixedStart = false
    var fixedEnd = false
    var validationError: ValidationError? = null
        set(value) {
            if (value == null || field == null) {
                field = value
            }
        }

    fun validate(): AddParkingForm {
        validationError = null
        val now = Date()
        if (start < now) {
            start = now
            if (fixedStart) {
                fixedStart = false
                validationError = ValidationError.START_IN_PAST
            }
            if (now > regime.end) {
                validationError = ValidationError.REGIME_IN_PAST
            }
        }
        if (start < regime.start) {
            start = regime.start
            fixedStart = false
            validationError = ValidationError.START_BEFORE_REGIME
        }
        if (start > regime.end) {
            start = regime.end
            fixedStart = false
            validationError = ValidationError.START_AFTER_REGIME
        }
        if (end < start) {
            fixedEnd = false
            end = start
            validationError = ValidationError.END_BEFORE_START
        }
        if (end > regime.end) {
            fixedEnd = false
            end = regime.end
            validationError = ValidationError.END_AFTER_REGIME
        }
        if (cost > balance) {
            minutes = (balance / (hourRate / 60.0)).toInt()
            validationError = null
            validationError = ValidationError.NOT_ENOUGH_BALANCE
        }
        return this
    }

    enum class ValidationError(val message: Int) {
        REGIME_IN_PAST     (R.string.parking_validation_regime_in_past),
        START_IN_PAST      (R.string.parking_validation_start_in_past),
        START_BEFORE_REGIME(R.string.parking_validation_start_before_regime),
        START_AFTER_REGIME (R.string.parking_validation_start_after_regime),
        END_BEFORE_START   (R.string.parking_validation_end_before_start),
        END_AFTER_REGIME   (R.string.parking_validation_end_after_regime),
        NOT_ENOUGH_BALANCE (R.string.parking_validation_not_enough_balance),
        ;
    }

}