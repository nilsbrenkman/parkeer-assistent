package nl.parkeerassistent.android.ui.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.parkeerassistent.android.data.Message
import nl.parkeerassistent.android.databinding.ItemMessageBinding

class MessageRecyclerViewAdapter(
    var values: MutableList<Message>
) : RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = values[position]
        holder.messageView.text = message.message
        holder.messageView.level = message.level
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        val messageView: MessageView = binding.message

        override fun toString(): String {
            return super.toString() + " '" + messageView.text + "'"
        }
    }

}