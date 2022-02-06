package nl.parkeerassistent.android.service

object ServiceUtil {

    fun mockDelay(delay: Long = 500) {
        Thread.sleep(delay)
    }

}

class Callback {
    var ok: (() -> Unit)? = null
    var fail: (() -> Unit)? = null

    fun perform(success: Boolean) {
        if (success) {
            ok?.invoke()
        } else {
            fail?.invoke()
        }
    }
}

inline fun callback(builder: Callback.() -> Unit) : Callback {
    return Callback().apply(builder)
}
