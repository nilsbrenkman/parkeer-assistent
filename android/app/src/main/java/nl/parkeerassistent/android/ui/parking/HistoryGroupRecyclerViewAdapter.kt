package nl.parkeerassistent.android.ui.parking

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.parkeerassistent.android.data.HistoryGroup
import nl.parkeerassistent.android.data.Parking
import nl.parkeerassistent.android.databinding.ItemHistoryGroupBinding
import nl.parkeerassistent.android.util.DateUtil.DateFormat.MonthYear
import java.util.*

class HistoryGroupRecyclerViewAdapter(
    var values: SortedMap<HistoryGroup, List<Parking>>
) : RecyclerView.Adapter<HistoryGroupRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHistoryGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = values.keys.elementAt(position)
        holder.groupView.text = MonthYear.format(group.date).uppercase()

        val history = values[group]?.sortedByDescending(Parking::startDate) ?: emptyList()
        with(holder.listView) {
            layoutManager = LinearLayoutManager(context)
            adapter = HistoryRecyclerViewAdapter(history)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemHistoryGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        val groupView: TextView = binding.historyGroup
        val listView: RecyclerView = binding.historyList

        override fun toString(): String {
            return super.toString() + " '" + groupView.text + "'"
        }
    }

}
