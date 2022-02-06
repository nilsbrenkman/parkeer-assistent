package nl.parkeerassistent.android.ui.picker

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import nl.parkeerassistent.android.ui.parking.ParkingViewModel
import nl.parkeerassistent.android.util.DateUtil
import nl.parkeerassistent.android.util.FragmentUtil
import java.util.*

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    lateinit var picker: Picker
    private lateinit var date: Date

    private var hour: Int = 0
    private var minute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FragmentUtil.restoreSaved(savedInstanceState, DATE_KEY) { bundle, key ->
            setValues(Date(bundle.getLong(key)))
        }
        FragmentUtil.restoreSaved(savedInstanceState, PICKER_KEY) { bundle, key ->
            picker = Picker.valueOf(bundle.getString(key, ""))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(requireActivity(), this, hour, minute, true)
    }

    fun setValues(date: Date) {
        this.date = date
        val c = Calendar.getInstance(DateUtil.amsterdam)
        c.time = date

        this.hour = c.get(Calendar.HOUR_OF_DAY)
        this.minute = c.get(Calendar.MINUTE)
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        if (this.hour != hour || this.minute != minute) {
            val parkingViewModel = ViewModelProvider(requireActivity())[ParkingViewModel::class.java]
            when (picker) {
                Picker.START -> parkingViewModel.setStart(hour, minute)
                Picker.END -> parkingViewModel.setEnd(hour, minute)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(DATE_KEY, date.time)
        outState.putString(PICKER_KEY, picker.name)
    }

    companion object {
        private const val DATE_KEY = "time-picker-fragment-date"
        private const val PICKER_KEY = "time-picker-fragment-picker"
    }

    enum class Picker {
        START,
        END,
    }

}