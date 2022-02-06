package nl.parkeerassistent.android.ui.message

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.Level

class MessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var level: Level? = Level.SUCCESS

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        level?.let { level ->
            val drawableState = super.onCreateDrawableState(extraSpace + 1)
            when (level) {
                Level.INFO    -> mergeDrawableStates(drawableState, intArrayOf(R.attr.level_info))
                Level.SUCCESS -> mergeDrawableStates(drawableState, intArrayOf(R.attr.level_success))
                Level.WARN    -> mergeDrawableStates(drawableState, intArrayOf(R.attr.level_warn))
                Level.ERROR   -> mergeDrawableStates(drawableState, intArrayOf(R.attr.level_error))
            }
            return drawableState
        }
        return super.onCreateDrawableState(extraSpace)
    }

}