package nl.parkeerassistent.android.ui.parking

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nl.parkeerassistent.android.data.Parking
import nl.parkeerassistent.android.databinding.ItemHistoryBinding
import nl.parkeerassistent.android.util.DateUtil.DateFormat
import nl.parkeerassistent.android.util.LicenseUtil
import nl.parkeerassistent.android.util.TextUtil

class HistoryRecyclerViewAdapter(
    var values: List<Parking>
) : RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.dowView.text = DateFormat.DayOfWeek.format(item.startDate)
        holder.dayView.text = DateFormat.Day.format(item.startDate)
        holder.licenseView.text = LicenseUtil.format(item.license)
        holder.costView.text = TextUtil.formatCost(item.cost, true)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        val dowView: TextView = binding.dow
        val dayView: TextView = binding.day
        val licenseView: TextView = binding.license
        val costView: TextView = binding.cost

        override fun toString(): String {
            return super.toString() + " '" + licenseView.text + "'"
        }
    }

}
