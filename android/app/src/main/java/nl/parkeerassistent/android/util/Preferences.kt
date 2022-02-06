package nl.parkeerassistent.android.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.GeneralSecurityException
import java.security.KeyStoreException

private const val PREFERENCES_KEY = "nl.parkeerassistent.android.preferences"
private const val SECURE_PREFERENCES_KEY = "nl.parkeerassistent.android.secure_preferences"

private fun Context.getPreferences(secure: Boolean): SharedPreferences? {
    if (secure) {
        return getSecurePreferences()
    }
    return getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
}

private fun Context.getSecurePreferences(): SharedPreferences? {
    val mainKey = MasterKey.Builder(this)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    return try {
        EncryptedSharedPreferences.create(
            this,
            SECURE_PREFERENCES_KEY,
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: KeyStoreException) {
        Log.e("KeyChain", "Could not load key", e)
        null
    } catch (e: GeneralSecurityException) {
        Log.e("KeyChain", "Could not generate key", e)
        null
    }
}

fun Context.getPreference(preference: Preference): String? {
    return getPreferences(preference.secure)?.getString(preference.key, null)
}

fun Context.setPreference(preference: Preference, value: String?) {
    val preferences = getPreferences(preference.secure) ?: return
    with(preferences.edit()) {
        putString(preference.key, value)
        apply()
    }
}

fun Context.getPreferenceOrDefault(preference: Preference, defaultFunction: () -> String): String {
    return getPreference(preference) ?: run {
        val defaultValue = defaultFunction()
        setPreference(preference, defaultValue)
        defaultValue
    }
}

enum class Preference(val key: String, val secure: Boolean = false) {
    VERSION("version"),
    UUID("uuid"),
    COOKIE_SESSION("cookie.session"),
    COOKIE_CUSTOMER("cookie.customer"),
    COOKIE_PERMIT("cookie.permit"),
    PAYMENT_AMOUNT("payment.amount"),
    PAYMENT_ISSUER("payment.issuer"),

    CREDENTIALS("credentials", true),
}
