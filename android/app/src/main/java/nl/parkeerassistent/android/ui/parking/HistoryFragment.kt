package nl.parkeerassistent.android.ui.parking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.HistoryGroup
import nl.parkeerassistent.android.ui.BackFragment
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.ui.user.UserFragment
import nl.parkeerassistent.android.util.hidden
import nl.parkeerassistent.android.util.ifElse
import java.util.*

@AndroidEntryPoint
class HistoryFragment : BaseFragment(), BackFragment {

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val listAdapter = HistoryGroupRecyclerViewAdapter(TreeMap())

        val loading = view.findViewById<ProgressBar>(R.id.loading)
        val empty = view.findViewById<TextView>(R.id.empty)
        val list = view.findViewById<RecyclerView>(R.id.history_group)

        with(list) {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }

        parkingViewModel.history.observe(viewLifecycleOwner) { history ->
            history?.isEmpty()?.ifElse({
                loading.hidden(true)
                empty.hidden(false)
            }, {
                listAdapter.values = history
                    .groupBy { h -> HistoryGroup(h.startDate) }
                    .toSortedMap(reverseOrder())
                listAdapter.notifyDataSetChanged()

                loading.hidden(true)
                list.hidden(false)
            })
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        parkingViewModel.getHistory()
    }

    override fun back() {
        loadFragment<UserFragment>()
    }

    override fun hideMenuItem(menuItemId: Int): Boolean {
        return R.id.history == menuItemId
    }

}