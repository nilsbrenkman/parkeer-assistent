package nl.parkeerassistent.android.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.ui.visitor.AddVisitorFragment
import nl.parkeerassistent.android.util.hideKeyboard
import nl.parkeerassistent.android.util.onClick

@AndroidEntryPoint
class UserFragment : BaseFragment() {

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        val addVisitor = view.findViewById<Button>(R.id.add_visitor)
        addVisitor.onClick {
            visitorViewModel.visitors.value?.let { visitors ->
                if (visitors.size > 8) {
                    messageViewModel.warn(getString(R.string.visitor_tooManyMsg))
                    return@onClick
                }
            }
            loadFragment<AddVisitorFragment>()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        hideKeyboard()
        userViewModel.getUser()
    }

    override fun hideMenuItem(menuItemId: Int): Boolean {
        return R.id.main == menuItemId
    }

}