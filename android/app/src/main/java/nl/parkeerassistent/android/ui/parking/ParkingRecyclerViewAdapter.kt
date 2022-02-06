package nl.parkeerassistent.android.ui.parking

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.Parking
import nl.parkeerassistent.android.databinding.ItemParkingBinding
import nl.parkeerassistent.android.util.DateUtil.DateFormat
import nl.parkeerassistent.android.util.DateUtil.formatParkingDuration
import nl.parkeerassistent.android.util.LicenseUtil
import nl.parkeerassistent.android.util.OnConfirm
import nl.parkeerassistent.android.util.hidden
import nl.parkeerassistent.android.util.menu
import java.util.*

class ParkingRecyclerViewAdapter(
    private val list: ParkingFragment.ParkingList,
    var values: MutableList<Parking>,
    private val stop: (Parking, OnConfirm) -> Unit
) : RecyclerView.Adapter<ParkingRecyclerViewAdapter.ViewHolder>() {

    val handler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemParkingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val parking = values[position]
        holder.licenseView.text = LicenseUtil.format(parking.license)
        holder.nameView.text = parking.name
        holder.menuView.menu(R.menu.parking) { item ->
            when (item.itemId) {
                R.id.delete -> {
                    stop(parking) { confirm ->
                        if (confirm) {
                            values.remove(parking)
                            notifyDataSetChanged()
                        }
                    }
               }
            }
        }
        if (list == ParkingFragment.ParkingList.ACTIVE) {
            holder.dateTimeView.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.background_datetime_active)
            holder.dateView.hidden(true)
            holder.endDate = parking.endDate
            handler.post(holder.updateTask)
        } else {
            holder.dateTimeView.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.background_datetime_scheduled)
            holder.dateView.text = DateFormat.DayMonth.format(parking.startDate)
            holder.timeView.text = DateFormat.Time.format(parking.startDate)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        handler.removeCallbacks(holder.updateTask)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemParkingBinding) : RecyclerView.ViewHolder(binding.root) {
        val licenseView: TextView = binding.license
        val nameView: TextView = binding.name
        val dateTimeView: LinearLayout = binding.dateTime
        val dateView: TextView = binding.date
        val timeView: TextView = binding.time
        val menuView: ImageButton = binding.menu

        lateinit var endDate: Date

        val updateTask: Runnable = object : Runnable {
            override fun run() {
                try {
                    val duration = endDate.time - Date().time
                    timeView.text = formatParkingDuration(duration)
                } finally {
                    handler.postDelayed(this, 1000)
                }
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + licenseView.text + "'"
        }
    }

}