package nl.parkeerassistent.android.ui.parking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.Visitor
import nl.parkeerassistent.android.service.callback
import nl.parkeerassistent.android.ui.BackFragment
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.ui.picker.DatePickerFragment
import nl.parkeerassistent.android.ui.picker.MinutePickerFragment
import nl.parkeerassistent.android.ui.picker.TimePickerFragment
import nl.parkeerassistent.android.ui.user.UserFragment
import nl.parkeerassistent.android.util.DateUtil
import nl.parkeerassistent.android.util.DateUtil.DateFormat
import nl.parkeerassistent.android.util.LicenseUtil
import nl.parkeerassistent.android.util.TextUtil
import nl.parkeerassistent.android.util.onClick

@AndroidEntryPoint
class AddParkingFragment : BaseFragment(), BackFragment {

    companion object {
        const val ARGS_VISITOR = "visitor"
    }

    private lateinit var visitor: Visitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitor = Json.decodeFromString(arguments?.getString(ARGS_VISITOR)!!)
        handler.postDelayed(updateTask, DateUtil.nextUpdate())
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTask)
    }

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_parking, container, false)

        val license = view.findViewById<TextView>(R.id.license)
        val name = view.findViewById<TextView>(R.id.name)

        val date = view.findViewById<TextView>(R.id.date)
        val start = view.findViewById<TextView>(R.id.start)
        val end = view.findViewById<TextView>(R.id.end)
        val minutes = view.findViewById<TextView>(R.id.minutes)
        val cost = view.findViewById<TextView>(R.id.cost)
        val add = view.findViewById<Button>(R.id.add)

        license.text = LicenseUtil.format(visitor.license)
        name.text = visitor.name

        parkingViewModel.addParkingForm.observe(viewLifecycleOwner) { form ->
            date.text = DateFormat.DayMonth.format(form.start)
            start.text = DateFormat.Time.format(form.start)
            minutes.text = form.minutes.toString()
            end.text = DateFormat.Time.format(form.end)
            cost.text = TextUtil.formatCost(form.cost, true)
            add.isEnabled = form.minutes > 0
        }

        parkingViewModel.validationError.observe(viewLifecycleOwner) { error ->
            error ?: return@observe
            messageViewModel.warn(getString(error.message))
        }

        date.onClick {
            parkingViewModel.addParkingForm.value?.let { form ->
                val datePicker = DatePickerFragment()
                datePicker.setValues(form.start)
                datePicker.show(requireActivity().supportFragmentManager, "datepicker")
            }
        }
        start.onClick {
            parkingViewModel.addParkingForm.value?.let { form ->
                val timePicker = TimePickerFragment()
                timePicker.picker = TimePickerFragment.Picker.START
                timePicker.setValues(form.start)
                timePicker.show(requireActivity().supportFragmentManager, "startpicker")
            }
        }
        end.onClick {
            parkingViewModel.addParkingForm.value?.let { form ->
                val timePicker = TimePickerFragment()
                timePicker.picker = TimePickerFragment.Picker.END
                timePicker.setValues(form.end)
                timePicker.show(requireActivity().supportFragmentManager, "endpicker")
            }
        }
        minutes.onClick {
            parkingViewModel.addParkingForm.value?.let { form ->
                val minutePicker = MinutePickerFragment()
                minutePicker.minutes = form.minutes
                minutePicker.show(requireActivity().supportFragmentManager, "minutepicker")
            }
        }
        add.onClick {
            parkingViewModel.startParking(visitor, callback {
                ok = {
                    userViewModel.getBalance()
                    loadFragment<UserFragment>()
                }
                fail = {
                    messageViewModel.error(getString(R.string.error_add_parking))
                }
            })
        }
        return view
    }

    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            try {
                parkingViewModel.updateForm()
            } finally {
                handler.postDelayed(this, DateUtil.nextUpdate())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        parkingViewModel.startForm()
    }

    override fun back() {
        loadFragment<UserFragment>()
    }

}