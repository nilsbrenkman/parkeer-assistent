package nl.parkeerassistent.android.util

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment

fun EditText.afterChange(afterChange: () -> Unit) {
    this.addTextChangedListener(AfterTextChangedWatcher {
        afterChange()
    })
}

fun EditText.formatted(formatter: (String) -> String) {
    this.addTextChangedListener(AfterTextChangedWatcher { e ->
        val before = e.toString()
        val after = formatter(before)
        if (before != after) {
            e.clear()
            e.append(after)
        }
    })
}

class AfterTextChangedWatcher(private val afterChange: (Editable) -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(editable: Editable?) {
        editable?.let {
            afterChange(editable)
        }
    }
}

fun View.onClick(action: () -> Unit) {
    this.setOnClickListener {
        action()
    }
}

fun View.show(show: Boolean) {
    hidden(! show)
}

fun View.hidden(hidden: Boolean) {
    if (hidden) {
        this.visibility = View.GONE
    } else {
        this.visibility = View.VISIBLE
    }
}

fun View.menu(resource: Int, onMenuItemClick: (MenuItem) -> Unit) {
    this.setOnClickListener {
        val popupMenu = PopupMenu(this.context, this)
        val menuInflater: MenuInflater = popupMenu.menuInflater
        menuInflater.inflate(resource, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            item?.let(onMenuItemClick)
            true
        }
        popupMenu.show()
    }
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

typealias OnConfirm = (Boolean) -> Unit