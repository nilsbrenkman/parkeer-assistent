package nl.parkeerassistent.android.ui.visitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.service.callback
import nl.parkeerassistent.android.ui.BackFragment
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.ui.user.UserFragment
import nl.parkeerassistent.android.util.LicenseUtil
import nl.parkeerassistent.android.util.afterChange
import nl.parkeerassistent.android.util.formatted
import nl.parkeerassistent.android.util.onClick

@AndroidEntryPoint
class AddVisitorFragment : BaseFragment(), BackFragment {

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_visitor, container, false)

        val license = view.findViewById<EditText>(R.id.license)
        val name = view.findViewById<EditText>(R.id.name)
        val add = view.findViewById<Button>(R.id.add)

        val dataChanged = fun() {
            add.isEnabled = license.text.toString().isNotEmpty() && name.text.toString().isNotEmpty()
        }

        license.formatted(LicenseUtil::format)
        license.afterChange(dataChanged)

        name.afterChange(dataChanged)

        add.onClick {
            add.isEnabled = false
            visitorViewModel.addVisitor(license.text.toString(), name.text.toString(), callback {
                ok = {
                    messageViewModel.success(getString(R.string.visitor_successMsg))
                    loadFragment<UserFragment>()
                }
                fail = {
                    messageViewModel.error(getString(R.string.visitor_errorMsg))
                    add.isEnabled = true
                }
            })
        }

        return view
    }

    override fun back() {
        loadFragment<UserFragment>()
    }

}