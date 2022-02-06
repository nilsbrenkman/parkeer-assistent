package nl.parkeerassistent.android.ui.visitor

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.Visitor
import nl.parkeerassistent.android.databinding.ItemVisitorBinding
import nl.parkeerassistent.android.util.LicenseUtil
import nl.parkeerassistent.android.util.OnConfirm
import nl.parkeerassistent.android.util.menu

class VisitorRecyclerViewAdapter(
    var values: MutableList<Visitor>,
    private val addParking: (Visitor) -> Unit,
    private val delete: (Visitor, OnConfirm) -> Unit
) : RecyclerView.Adapter<VisitorRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemVisitorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visitor = values[position]
        holder.licenseView.text = LicenseUtil.format(visitor.license)
        holder.nameView.text = visitor.name
        holder.menuView.menu(R.menu.visitor) { item ->
            when (item.itemId) {
                R.id.add_parking -> addParking(visitor)
                R.id.delete -> {
                    delete(visitor) { confirm ->
                        if (confirm) {
                            values.remove(visitor)
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }
        holder.itemView.setOnClickListener { addParking(visitor) }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemVisitorBinding) : RecyclerView.ViewHolder(binding.root) {
        val licenseView: TextView = binding.license
        val nameView: TextView = binding.name
        val menuView: ImageButton = binding.menu

        override fun toString(): String {
            return super.toString() + " '" + licenseView.text + "'"
        }
    }

}