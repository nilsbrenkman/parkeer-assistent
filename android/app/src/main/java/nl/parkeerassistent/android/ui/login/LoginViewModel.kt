package nl.parkeerassistent.android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import nl.parkeerassistent.android.data.LoginRepository
import nl.parkeerassistent.android.service.Callback
import nl.parkeerassistent.android.util.KeyChain
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val loginRepository: LoginRepository
) : ViewModel() {

    var storedLogin = false

    private val _loggedIn = MutableLiveData<Boolean>()
    val loggedIn: LiveData<Boolean> = _loggedIn

    var credentials: KeyChain.Credentials? = null

    private var _autoLogin: Boolean = true
    val autoLogin: Boolean
        get() {
            if (_autoLogin) {
                _autoLogin = false
                return true
            }
            return false
        }

    fun isLoggedIn(callback: Callback? = null) {
        viewModelScope.launch {
            val response = loginRepository.isLoggedIn()
            callback?.perform(response.success)
            if (_loggedIn.value != response.success){
                _loggedIn.value = response.success
            }
        }
    }

    fun login(username: String, password: String, callback: Callback? = null) {
        viewModelScope.launch {
            val response = loginRepository.login(username, password)
            callback?.perform(response.success)
            isLoggedIn()
        }
    }

    fun logout() {
        viewModelScope.launch {
            loginRepository.logout()
            isLoggedIn()
        }
    }

    fun credentials(): KeyChain.Credentials? {
        credentials?.let { c ->
            credentials = null
            return c
        }
        return null
    }

}