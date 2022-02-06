package nl.parkeerassistent.android.ui.parking

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.Parking
import nl.parkeerassistent.android.data.ParkingResponse
import nl.parkeerassistent.android.service.callback
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.ui.SwipeToDelete
import nl.parkeerassistent.android.util.FragmentUtil
import nl.parkeerassistent.android.util.OnConfirm

@AndroidEntryPoint
class ParkingFragment : BaseFragment() {

    companion object {
        private const val PARKING_LIST_KEY = "ParkingList"
    }

    private lateinit var parkingList: ParkingList

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ParkingFragment_MembersInjector)
        parkingList = ParkingList.values()[attributes.getInt(R.styleable.ParkingFragment_MembersInjector_parkingList, 0)]
        attributes.recycle()
    }

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parking, container, false)

        FragmentUtil.restoreSaved(bundle, PARKING_LIST_KEY) { b, key ->
            parkingList = ParkingList.valueOf(b.getString(key, ""))
        }

        val stopParking = { parking: Parking, onConfirm: OnConfirm ->
            val alert = AlertDialog.Builder(context, R.style.ParkeerAssistent_DeleteDialog)
                .setTitle(getString(R.string.parking_stop) + "?")
                .setNeutralButton(R.string.common_cancel) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    onConfirm(false)
                }
                .setNegativeButton(R.string.common_stop) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    onConfirm(true)
                    parkingViewModel.stopParking(parking, callback {
                        ok = { userViewModel.getBalance() }
                    })
                }
                .create()
            alert.show()
        }

        val listAdapter = ParkingRecyclerViewAdapter(parkingList, mutableListOf(), stopParking)

        val header = view.findViewById<TextView>(R.id.parking_header)
        val list = view.findViewById<RecyclerView>(R.id.parking_list)

        header.text = "${getString(parkingList.header)}:"

        with(list) {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter

            SwipeToDelete(requireContext()) { position ->
                val parking = listAdapter.values[position]
                stopParking(parking) { confirm ->
                    if (confirm) {
                        listAdapter.values.remove(parking)
                    }
                    listAdapter.notifyDataSetChanged()
                }
            }.attach(this)
        }

        parkingViewModel.parking.observe(viewLifecycleOwner) { parking ->
            listAdapter.values = parkingList.list(parking).toMutableList()
            listAdapter.notifyDataSetChanged()
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PARKING_LIST_KEY, parkingList.name)
    }

    enum class ParkingList(val list: (ParkingResponse) -> List<Parking>, val header: Int) {
        ACTIVE   (ParkingResponse::active,    R.string.parking_active),
        SCHEDULED(ParkingResponse::scheduled, R.string.parking_scheduled),
        ;
    }

}