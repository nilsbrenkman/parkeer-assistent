package nl.parkeerassistent.android.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import nl.parkeerassistent.android.R

class SwipeToDelete(context: Context, private val onDelete: (Int) -> Unit) : ItemTouchHelper.Callback() {

    private val backgroundColor: ColorDrawable = ColorDrawable()
    private val icon: Drawable?
    private var itemHeight = 0
    private lateinit var background: Bitmap

    init {
        backgroundColor.color = ContextCompat.getColor(context, R.color.danger)
        icon = ContextCompat.getDrawable(context, R.drawable.icon_delete_sweep)
    }

    fun attach(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (dX.toInt() == 0 && ! isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        val itemView = viewHolder.itemView
        if (itemHeight != itemView.height) {
            createBackground(itemView)
        }

        c.drawBitmap(background,
            Rect(itemView.width + dX.toInt(), 0, itemView.width, itemView.height),
            Rect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom),
            Paint()
        )

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun createBackground(itemView: View) {
        background = Bitmap.createBitmap(itemView.width, itemView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(background)

        backgroundColor.setBounds(0, 0, itemView.width, itemView.height)
        backgroundColor.draw(canvas)

        icon?.let { i ->
            val iconMargin = itemView.height / 4
            val scale = (itemView.height - iconMargin * 2).toDouble() / i.intrinsicHeight
            val iconWidth = (i.intrinsicWidth * scale).toInt()

            i.setBounds(
                itemView.width - iconWidth - iconMargin,
                iconMargin,
                itemView.width - iconMargin,
                itemView.height - iconMargin
            )
            i.draw(canvas)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        onDelete(position)
    }

}