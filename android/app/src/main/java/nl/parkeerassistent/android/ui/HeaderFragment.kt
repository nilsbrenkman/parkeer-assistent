package nl.parkeerassistent.android.ui

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.iterator
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.ui.parking.HistoryFragment
import nl.parkeerassistent.android.ui.payment.PaymentFragment
import nl.parkeerassistent.android.ui.user.UserFragment
import nl.parkeerassistent.android.util.hidden
import nl.parkeerassistent.android.util.onClick


@AndroidEntryPoint
class HeaderFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener {

    @SuppressLint("RestrictedApi")
    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_header, container, false)

        val logoContainer = view.findViewById<LinearLayout>(R.id.header_logo_container)
        val logo = view.findViewById<ImageView>(R.id.header_logo)
        val menu = view.findViewById<ImageButton>(R.id.menu)

        logoContainer.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        logoContainer.layoutTransition.setDuration(LayoutTransition.CHANGING, 300)
        logoContainer.layoutTransition.setInterpolator(LayoutTransition.CHANGING, LinearInterpolator(context, null))

        loginViewModel.loggedIn.observe(viewLifecycleOwner) { loggedIn ->
            menu.hidden(! loggedIn)
            moveLogo(logoContainer, loggedIn)
        }

        logo.onClick {
            appViewModel.toggleInfo()
        }

        menu.onClick {
            val popupMenu = PopupMenu(requireContext(), menu)
            val menuInflater: MenuInflater = popupMenu.menuInflater
            menuInflater.inflate(R.menu.main, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(this)
            activity?.supportFragmentManager?.let { fragmentManager ->
                val mainFragment = fragmentManager.findFragmentById(R.id.fragment_content_container)
                (mainFragment as? BaseFragment)?.let { fragment ->
                    popupMenu.menu.iterator().forEach { item ->
                        item.isVisible = !fragment.hideMenuItem(item.itemId)
                    }
                }
            }

            val menuHelper = MenuPopupHelper(requireContext(), popupMenu.menu as MenuBuilder, menu)
            menuHelper.setForceShowIcon(true)
            menuHelper.show()
        }

        return view
    }

    private fun moveLogo(layout: LinearLayout, left: Boolean) {
        if (left) {
            layout.gravity = Gravity.START
        } else {
            layout.gravity = Gravity.CENTER_HORIZONTAL
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.main    -> loadFragment<UserFragment>()
            R.id.history -> loadFragment<HistoryFragment>()
            R.id.payment -> loadFragment<PaymentFragment>()
            R.id.logout  -> loginViewModel.logout()
        }
        return true
    }

}