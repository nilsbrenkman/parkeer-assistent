package nl.parkeerassistent.android.ui.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.SwitchCompat
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.service.callback
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.util.KeyChain
import nl.parkeerassistent.android.util.afterChange
import nl.parkeerassistent.android.util.onClick
import nl.parkeerassistent.android.util.show

@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val username = view.findViewById<EditText>(R.id.username)
        val password = view.findViewById<EditText>(R.id.password)
        val login = view.findViewById<Button>(R.id.login)
        val remember = view.findViewById<SwitchCompat>(R.id.remember_credentials)
        val authenticate = view.findViewById<ImageButton>(R.id.authenticate)

        val dataChanged = fun() {
            login.isEnabled = username.text.toString().isNotEmpty() && password.text.toString().isNotEmpty()
        }

        username.afterChange(dataChanged)
        password.afterChange(dataChanged)

        login.onClick {
            loginViewModel.login(username.text.toString(), password.text.toString(), callback {
                ok = {
                    if (remember.isChecked) {
                        KeyChain.storeCredentials(activity, username.text.toString(), password.text.toString())
                    }
                }
                fail = {
                    messageViewModel.warn("Login failed")
                }
            })
        }

        val canAuthenticate = KeyChain.canAuthenticate(context)
        val hasCredentials = KeyChain.hasCredentials(activity)

        val onCredentials: (KeyChain.Credentials) -> Unit = { credentials ->
            loginViewModel.credentials = null
            username.setText(credentials.username)
            password.setText(credentials.password)
            login.performClick()
        }

        loginViewModel.credentials()?.run {
            onCredentials(this)
        } ?: run {
            if (loginViewModel.autoLogin && canAuthenticate && hasCredentials) {
                startAuthentication(onCredentials)
            }
        }

        with(remember.parent as View) {
            show(canAuthenticate)
        }

        authenticate.show(canAuthenticate && hasCredentials)
        authenticate.onClick {
            startAuthentication(onCredentials)
        }

        return view
    }

    fun startAuthentication(onCredentials: (KeyChain.Credentials) -> Unit) {
        val executor = context?.run { ContextCompat.getMainExecutor(this) } ?: return
        val biometricPrompt = BiometricPrompt(this, executor, LoginAuthenticationCallback { result ->
            KeyChain.loadCredentials(activity) { credentials ->
                loginViewModel.credentials = credentials
                onCredentials(credentials)
            }
        })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.login_authenticate_title))
            .setSubtitle(getString(R.string.login_authenticate_subtitle))
            .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    class LoginAuthenticationCallback(val onAuthenticated: (BiometricPrompt.AuthenticationResult) -> Unit) : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthenticated(result)
        }
    }

}
