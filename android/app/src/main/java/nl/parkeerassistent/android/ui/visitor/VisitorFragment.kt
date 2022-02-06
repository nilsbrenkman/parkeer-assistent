package nl.parkeerassistent.android.ui.visitor

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.Visitor
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.ui.SwipeToDelete
import nl.parkeerassistent.android.ui.parking.AddParkingFragment
import nl.parkeerassistent.android.util.OnConfirm
import nl.parkeerassistent.android.util.hidden
import nl.parkeerassistent.android.util.ifElse

@AndroidEntryPoint
class VisitorFragment : BaseFragment() {

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_visitor, container, false)

        val deleteVisitor = { visitor: Visitor, onConfirm: OnConfirm ->
            val alert = AlertDialog.Builder(context, R.style.ParkeerAssistent_DeleteDialog)
                .setTitle(getString(R.string.visitor_delete) + "?")
                .setNeutralButton(R.string.common_cancel) { _, _ ->
                    onConfirm(false)
                }
                .setNegativeButton(R.string.common_delete) { _, _ ->
                    onConfirm(true)
                    visitorViewModel.deleteVisitor(visitor)
                }
                .create()
            alert.show()
        }

        val listAdapter = VisitorRecyclerViewAdapter(mutableListOf(),
                addParking = { visitor -> loadFragment<AddParkingFragment>(bundleOf(Pair(AddParkingFragment.ARGS_VISITOR, Json.encodeToString(visitor)))) },
                delete     = deleteVisitor )

        val loading = view.findViewById<ProgressBar>(R.id.loading)
        val empty = view.findViewById<TextView>(R.id.empty)
        val list = view.findViewById<RecyclerView>(R.id.visitor_list)

        with(list) {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter

            SwipeToDelete(requireContext()) { position ->
                val visitor = listAdapter.values[position]
                deleteVisitor(visitor) { confirm ->
                    if (confirm) {
                        listAdapter.values.remove(visitor)
                    }
                    listAdapter.notifyDataSetChanged()
                }
            }.attach(this)
        }

        visitorViewModel.visitors.observe(viewLifecycleOwner) { visitors ->
            visitors.isEmpty().ifElse({
                empty.hidden(false)
                list.hidden(true)
            }, {
                empty.hidden(true)
                list.hidden(false)
                listAdapter.values = visitors.toMutableList()
                listAdapter.notifyDataSetChanged()
            })
        }

        visitorViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            loading.hidden(! isLoading)
            if (isLoading) {
                empty.hidden(true)
                list.hidden(true)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        visitorViewModel.getVisitors()
    }

}