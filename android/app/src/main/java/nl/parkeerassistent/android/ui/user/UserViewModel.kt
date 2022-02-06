package nl.parkeerassistent.android.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import nl.parkeerassistent.android.data.Regime
import nl.parkeerassistent.android.data.UserRepository
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    val userRepository: UserRepository,
) : ViewModel() {

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> = _balance

    private var hourRate: Double? = null

    private var regime: Regime? = null

    fun getUser() {
        viewModelScope.launch {
            val user = userRepository.getUser()
            _balance.value = user.balance
            hourRate = user.hourRate
            regime = user.regime
        }
    }

    fun getBalance() {
        viewModelScope.launch {
            _balance.value = userRepository.getBalance()
        }
    }

}