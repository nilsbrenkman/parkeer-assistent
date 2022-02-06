package nl.parkeerassistent.android.ui.visitor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.parkeerassistent.android.data.Visitor
import nl.parkeerassistent.android.data.VisitorRepository
import nl.parkeerassistent.android.service.Callback
import javax.inject.Inject

@HiltViewModel
class VisitorViewModel @Inject constructor(
    val visitorRepository: VisitorRepository,
) : ViewModel() {

    private val _visitors = object : MutableLiveData<List<Visitor>>() {
        override fun setValue(value: List<Visitor>?) {
            super.setValue(value)
            _loading.value = false
        }
    }
    val visitors: LiveData<List<Visitor>> = _visitors

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun getVisitors() {
        viewModelScope.launch {
            _visitors.value = visitorRepository.getVisitors()
        }
    }

    fun addVisitor(license: String, name: String, callback: Callback? = null) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                visitorRepository.visitorService.addVisitor(license, name)
            }
            if (response.success) {
                visitorRepository.state.visitors = null
                _loading.value = true
            }
            callback?.perform(response.success)
        }
    }

    fun deleteVisitor(visitor: Visitor, callback: Callback? = null) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                visitorRepository.visitorService.deleteVisitor(visitor)
            }
            if (response.success) {
                visitorRepository.state.visitors = null
                _visitors.value = visitorRepository.getVisitors()
            }
            callback?.perform(response.success)
        }
    }

}