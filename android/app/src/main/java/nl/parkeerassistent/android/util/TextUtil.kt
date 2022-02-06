package nl.parkeerassistent.android.util

object TextUtil {

    fun formatCost(cost: Double, currency: Boolean = false): String {
        if (currency) {
            return "€ ${formatCost(cost)}"
        }
        return "%.2f".format(cost)
    }

}

fun Boolean.ifElse(ifTrue: () -> Unit, ifFalse: (() -> Unit)? = null) {
    if (this) {
        ifTrue.invoke()
    } else {
        ifFalse?.invoke()
    }
}
