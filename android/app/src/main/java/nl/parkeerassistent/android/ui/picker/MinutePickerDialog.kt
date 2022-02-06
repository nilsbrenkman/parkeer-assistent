package nl.parkeerassistent.android.ui.picker

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import nl.parkeerassistent.android.R

class MinutePickerDialog(
    context: Context,
    private val minuteSetListener: OnMinuteSetListener,
    minutes: Int
) : AlertDialog(context, R.style.ParkeerAssistent_MinutePickerDialog), DialogInterface.OnClickListener {

    interface OnMinuteSetListener {
        fun onMinuteSet(minutes: Int)
    }

    private val hourPicker: NumberPicker
    private val minutePicker: NumberPicker

    init {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_minute_picker, null)
        setView(view)

        hourPicker = view.findViewById(R.id.minute_picker_hour)
        minutePicker = view.findViewById(R.id.minute_picker_minute)

        configureNumberPicker(hourPicker, 0, 24, false)
        configureNumberPicker(minutePicker, 0, 60, true)

        setMinutes(minutes)

        setButton(BUTTON_POSITIVE, context.getString(R.string.common_ok), this)
        setButton(BUTTON_NEUTRAL, context.getString(R.string.common_cancel), this)
    }

    private fun configureNumberPicker(numberPicker: NumberPicker, minValue: Int, maxValue: Int, wrapSelectorWheel: Boolean) {
        with(numberPicker) {
            this.minValue = minValue
            this.maxValue = maxValue
            this.wrapSelectorWheel = wrapSelectorWheel
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            BUTTON_POSITIVE -> minuteSetListener.onMinuteSet(getMinutes())
            BUTTON_NEUTRAL -> cancel()
        }
    }

    private fun getMinutes(): Int {
        return hourPicker.value * 60 + minutePicker.value
    }

    private fun setMinutes(minutes: Int) {
        hourPicker.value = minutes / 60
        minutePicker.value = minutes % 60
    }

    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putInt(MINUTE_KEY, getMinutes())
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val minutes = savedInstanceState.getInt(MINUTE_KEY)
        setMinutes(minutes)
    }

    companion object {
        private const val MINUTE_KEY = "minutes"
    }


}