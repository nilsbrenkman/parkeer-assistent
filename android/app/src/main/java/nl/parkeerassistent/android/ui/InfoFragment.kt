package nl.parkeerassistent.android.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.BuildConfig
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.util.onClick

@AndroidEntryPoint
class InfoFragment : BaseFragment() {

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)

        val infoContainer = view.findViewById<LinearLayout>(R.id.info_container)
        val close = view.findViewById<ImageButton>(R.id.close)

        for (index in 0 until infoContainer.childCount) {
            val info = infoContainer.getChildAt(index)
            if (info is TextView) {
                info.movementMethod = LinkMovementMethod.getInstance()
            }
        }

        val version = view.findViewById<TextView>(R.id.version)
        version.text = getString(R.string.info_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        view.onClick {
            appViewModel.toggleInfo()
        }

        close.onClick {
            appViewModel.toggleInfo()
        }

        return view
    }

}