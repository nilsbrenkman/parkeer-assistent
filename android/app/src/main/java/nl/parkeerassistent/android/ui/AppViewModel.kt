package nl.parkeerassistent.android.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {

    private val _showInfo = MutableLiveData<Boolean>()
    val showInfo: LiveData<Boolean> = _showInfo

    fun toggleInfo() {
        _showInfo.value = _showInfo.value?.not() ?: true
    }

}