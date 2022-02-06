package nl.parkeerassistent.android.ui.picker

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import nl.parkeerassistent.android.ui.parking.ParkingViewModel
import nl.parkeerassistent.android.util.DateUtil
import nl.parkeerassistent.android.util.FragmentUtil
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var date: Date

    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentUtil.restoreSaved(savedInstanceState, DATE_KEY) { bundle, key ->
            setValues(Date(bundle.getLong(key)))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = DatePickerDialog(requireActivity(), this, year, month, day)
        dialog.datePicker.minDate = Date().time
        return dialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        if (this.year != year || this.month != month || this.day != day) {
            val parkingViewModel = ViewModelProvider(requireActivity())[ParkingViewModel::class.java]
            parkingViewModel.setDate(year, month, day)
        }
    }

    fun setValues(date: Date) {
        this.date = date
        val c = Calendar.getInstance(DateUtil.amsterdam)
        c.time = date

        this.year = c.get(Calendar.YEAR)
        this.month = c.get(Calendar.MONTH)
        this.day = c.get(Calendar.DAY_OF_MONTH)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(DATE_KEY, date.time)
    }

    companion object {
        private const val DATE_KEY = "date-picker-fragment-date"
    }

}