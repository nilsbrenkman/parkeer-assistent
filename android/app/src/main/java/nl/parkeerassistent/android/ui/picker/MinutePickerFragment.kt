package nl.parkeerassistent.android.ui.picker

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import nl.parkeerassistent.android.ui.parking.ParkingViewModel
import nl.parkeerassistent.android.util.FragmentUtil

class MinutePickerFragment : DialogFragment(), MinutePickerDialog.OnMinuteSetListener {

    var minutes: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FragmentUtil.restoreSaved(savedInstanceState, MINUTE_KEY) { bundle, key ->
            minutes = bundle.getInt(key)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MinutePickerDialog(requireActivity(), this, minutes)
    }

    override fun onMinuteSet(minutes: Int) {
        val parkingViewModel = ViewModelProvider(requireActivity())[ParkingViewModel::class.java]
        parkingViewModel.setMinutes(minutes)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(MINUTE_KEY, minutes)
    }

    companion object {
        private const val MINUTE_KEY = "minute-picker-fragment"
    }

}