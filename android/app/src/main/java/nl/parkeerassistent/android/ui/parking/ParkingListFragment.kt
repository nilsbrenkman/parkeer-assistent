package nl.parkeerassistent.android.ui.parking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.ParkingResponse
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.util.hidden
import java.util.*

@AndroidEntryPoint
class ParkingListFragment : BaseFragment() {

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parking_list, container, false)

        val loading = view.findViewById<ProgressBar>(R.id.loading)
        val empty = view.findViewById<TextView>(R.id.empty)
        val active = view.findViewById<View>(R.id.active)
        val scheduled = view.findViewById<View>(R.id.scheduled)

        parkingViewModel.parking.observe(viewLifecycleOwner) { p ->
            empty.hidden(p.active.isNotEmpty() || p.scheduled.isNotEmpty() )
            active.hidden(p.active.isEmpty())
            scheduled.hidden(p.scheduled.isEmpty())
            scheduleUpdate(p)
            parkingViewModel.notificationUtil.scheduleNotifications(context, p)
        }

        parkingViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            loading.hidden(! isLoading)
            if (isLoading) {
                empty.hidden(true)
                active.hidden(true)
                scheduled.hidden(true)
            }
        }

        return view
    }

    private fun scheduleUpdate(parking: ParkingResponse) {
        val updates = parking.active.map { p -> p.endDate.time }.toMutableList()
        updates.addAll(parking.scheduled.map { p -> p.startDate.time })
        val next = updates.minOfOrNull { u -> u }
        next?.let { n ->
            val delay = n - Date().time + 500
            if (delay > 0) {
                handler.postDelayed(updateTask, delay)
            } else {
                handler.post(updateTask)
            }
        }
    }

    private val updateTask: Runnable = Runnable {
        parkingViewModel.parkingRepository.state.parking = null
        parkingViewModel.getParking()
    }

    override fun onStart() {
        super.onStart()
        parkingViewModel.getParking()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTask)
    }

}