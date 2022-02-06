package nl.parkeerassistent.android.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.databinding.ActivityAppBinding
import nl.parkeerassistent.android.service.ApiClient
import nl.parkeerassistent.android.service.NetworkException
import nl.parkeerassistent.android.service.ServiceException
import nl.parkeerassistent.android.service.callback
import nl.parkeerassistent.android.ui.login.LoginFragment
import nl.parkeerassistent.android.ui.login.LoginViewModel
import nl.parkeerassistent.android.ui.message.MessageViewModel
import nl.parkeerassistent.android.ui.payment.PaymentFragment
import nl.parkeerassistent.android.ui.payment.PaymentViewModel
import nl.parkeerassistent.android.ui.user.UserFragment
import nl.parkeerassistent.android.util.ExceptionHandler
import nl.parkeerassistent.android.util.hidden
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    @Inject lateinit var apiClient: ApiClient

    private lateinit var appViewModel: AppViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var messageViewModel: MessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        paymentViewModel = ViewModelProvider(this)[PaymentViewModel::class.java]
        messageViewModel = ViewModelProvider(this)[MessageViewModel::class.java]

        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val balanceFragment = binding.fragmentBalanceContainer
        val infoFragment = binding.fragmentInfoContainer

        appViewModel.showInfo.observe(this@AppActivity) { showInfo ->
            infoFragment.hidden(! showInfo)
        }

        loginViewModel.loggedIn.observe(this@AppActivity) { loggedIn ->
            if (loginViewModel.storedLogin) {
                loginViewModel.storedLogin = false
                if (loggedIn) return@observe
            }

            balanceFragment.hidden(! loggedIn)
            if (loggedIn) {
                supportFragmentManager.commit {
                    paymentViewModel.transactionId?.let {
                        replace<PaymentFragment>(R.id.fragment_content_container)
                        return@commit
                    }
                    replace<UserFragment>(R.id.fragment_content_container)
                }
            } else {
                supportFragmentManager.commit {
                    replace<LoginFragment>(R.id.fragment_content_container)
                }
            }
        }

    }

    fun handleException(e: Throwable) {
        if (e is ServiceException) {
            when (e.code) {
                403 -> {
                    loginViewModel.loginRepository.state.loggedIn = null
                    loginViewModel.isLoggedIn(callback {
                        fail = {
                            loginViewModel.loginRepository.state.reset()
                            messageViewModel.warn(getString(R.string.error_authentication))
                        }
                    })
                    return
                }
                else -> {}
            }
        }
        if (e is NetworkException) {
            Log.w("AppActivity", "Connection error", e)
            messageViewModel.error(getString(R.string.error_connection))
            return
        }
        Log.e("AppActivity", "Unexpected error", e)
        messageViewModel.error(getString(R.string.common_error))
    }

    override fun onStart() {
        super.onStart()
        loginViewModel.isLoggedIn()
    }

    override fun onPause() {
        super.onPause()
        loginViewModel.storedLogin = loginViewModel.loggedIn.value ?: false
    }

    override fun onBackPressed() {
        val mainFragment = supportFragmentManager.findFragmentById(R.id.fragment_content_container)
        (mainFragment as? BackFragment)?.let { backFragment ->
            backFragment.back()
            return
        }
        super.onBackPressed()
    }

}