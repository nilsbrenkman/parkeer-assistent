package nl.parkeerassistent.android.ui.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.ui.BaseFragment

@AndroidEntryPoint
class MessageFragment : BaseFragment() {

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)

        val list = view.findViewById<RecyclerView>(R.id.message_list)
        val listAdapter = MessageRecyclerViewAdapter(mutableListOf())

        with(list) {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }

        messageViewModel.messages.observe(viewLifecycleOwner) { messages ->
            listAdapter.values = messages.toMutableList()
            listAdapter.notifyDataSetChanged()
        }

        return view
    }
}