package nl.parkeerassistent.android.util

import android.os.Bundle

object FragmentUtil {


    fun restoreSaved(bundle: Bundle?, key: String, restore: (Bundle, String) -> Unit) {
        bundle ?: return
        if (bundle.containsKey(key)) {
            restore(bundle, key)
        }
    }

}