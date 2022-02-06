package nl.parkeerassistent.android.util

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object KeyChain {

    fun canAuthenticate(context: Context?): Boolean {
        context ?: return false
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL) == BIOMETRIC_SUCCESS
    }

    fun hasCredentials(context: Context?): Boolean {
        return context?.getPreference(Preference.CREDENTIALS) != null
    }

    fun loadCredentials(context: Context?, load: (credentials: Credentials) -> Unit) {
        val credentialsPreference = context?.getPreference(Preference.CREDENTIALS) ?: return
        val credentials = Json.decodeFromString<Credentials>(credentialsPreference)
        load(credentials)
    }

    fun storeCredentials(context: Context?, username: String, password: String) {
        try {
            val credentials = Json.encodeToString(Credentials(username, password))
            context?.setPreference(Preference.CREDENTIALS, credentials)
        } catch (e: Exception) {
            Log.e("KeyChain", "Unable to store credentials", e)
        }
    }

    @Serializable
    data class Credentials(
        val username: String,
        val password: String,
    )

}