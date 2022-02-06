package nl.parkeerassistent.android.ui.message

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import nl.parkeerassistent.android.data.Level
import nl.parkeerassistent.android.data.Message
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor() : ViewModel() {

    var handler: Handler = Handler(Looper.getMainLooper())

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    init {
        _messages.value = emptyList()
    }

    fun info(message: String) {
        addMessage(message, Level.INFO)
    }

    fun success(message: String) {
        addMessage(message, Level.SUCCESS)
    }

    fun warn(message: String) {
        addMessage(message, Level.WARN)
    }

    fun error(message: String) {
        addMessage(message, Level.ERROR)
    }

    private fun addMessage(message: String, level: Level) {
        val m = Message(message, level)
        val list = _messages.value?.toMutableList() ?: mutableListOf()
        list.add(m)
        _messages.value = list
        handler.postDelayed({
            val fList = _messages.value?.toMutableList() ?: mutableListOf()
            if (fList.remove(m)) {
                _messages.value = fList
            }
        }, 5000)
    }

}