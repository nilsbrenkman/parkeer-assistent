package nl.parkeerassistent.android.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.ui.login.LoginViewModel
import nl.parkeerassistent.android.ui.message.MessageViewModel
import nl.parkeerassistent.android.ui.parking.ParkingViewModel
import nl.parkeerassistent.android.ui.payment.PaymentViewModel
import nl.parkeerassistent.android.ui.user.UserViewModel
import nl.parkeerassistent.android.ui.visitor.VisitorViewModel

@AndroidEntryPoint
abstract class BaseFragment : Fragment() {

    lateinit var appViewModel: AppViewModel
        private set
    lateinit var loginViewModel: LoginViewModel
        private set
    lateinit var userViewModel: UserViewModel
        private set
    lateinit var visitorViewModel: VisitorViewModel
        private set
    lateinit var parkingViewModel: ParkingViewModel
        private set
    lateinit var paymentViewModel: PaymentViewModel
        private set
    lateinit var messageViewModel: MessageViewModel
        private set

    val handler = Handler(Looper.getMainLooper())

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = requireActivity()

        appViewModel     = ViewModelProvider(activity)[AppViewModel::class.java]
        loginViewModel   = ViewModelProvider(activity)[LoginViewModel::class.java]
        userViewModel    = ViewModelProvider(activity)[UserViewModel::class.java]
        visitorViewModel = ViewModelProvider(activity)[VisitorViewModel::class.java]
        parkingViewModel = ViewModelProvider(activity)[ParkingViewModel::class.java]
        paymentViewModel = ViewModelProvider(activity)[PaymentViewModel::class.java]
        messageViewModel = ViewModelProvider(activity)[MessageViewModel::class.java]

        return createFragment(inflater, container, savedInstanceState)
    }

    abstract fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View?

    open fun hideMenuItem(menuItemId: Int): Boolean {
        return false
    }

    protected inline fun <reified F : Fragment> loadFragment(bundle: Bundle? = null) {
        activity?.supportFragmentManager?.commit {
            replace<F>(R.id.fragment_content_container, args = bundle)
        }
    }

}