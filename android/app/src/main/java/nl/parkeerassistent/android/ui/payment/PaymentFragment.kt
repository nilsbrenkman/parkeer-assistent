package nl.parkeerassistent.android.ui.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.ui.BackFragment
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.ui.LoadingFragment
import nl.parkeerassistent.android.ui.user.UserFragment
import nl.parkeerassistent.android.util.Preference
import nl.parkeerassistent.android.util.getPreference
import nl.parkeerassistent.android.util.hidden
import nl.parkeerassistent.android.util.onClick
import nl.parkeerassistent.android.util.setPreference
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class PaymentFragment : BaseFragment(), BackFragment {

    private lateinit var dialog: AlertDialog

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        val loading = view.findViewById<ProgressBar>(R.id.loading)
        val form = view.findViewById<LinearLayout>(R.id.payment_container)
        val amount = view.findViewById<Spinner>(R.id.amount)
        val bank = view.findViewById<Spinner>(R.id.bank)
        val start = view.findViewById<Button>(R.id.start)

        dialog = AlertDialog.Builder(requireContext(), R.style.ParkeerAssistent_CancelDialog)
            .setMessage(R.string.payment_pendingMsg)
            .setNegativeButton(R.string.common_cancel) { _, _ ->
                paymentViewModel.transactionId = null
            }
            .create()

        paymentViewModel.ideal.observe(viewLifecycleOwner) { ideal ->
            with (amount) {
                adapter = ArrayAdapter(view.context, R.layout.support_simple_spinner_dropdown_item, ideal.amounts)
                onItemSelectedListener = paymentViewModel.selectedAmount
                context.getPreference(Preference.PAYMENT_AMOUNT)?.let { amountPreference ->
                    val position = ideal.amounts.indexOf(amountPreference)
                    if (position >=0) {
                        setSelection(position)
                    }
                }
            }
            with (bank) {
                adapter = ArrayAdapter(view.context, R.layout.support_simple_spinner_dropdown_item, ideal.issuers)
                onItemSelectedListener = paymentViewModel.selectedBank
                context.getPreference(Preference.PAYMENT_ISSUER)?.let { issuerPreference ->
                    val position = ideal.issuers.map { i -> i.issuerId }.indexOf(issuerPreference)
                    if (position >=0) {
                        setSelection(position)
                    }
                }
            }
            loading.hidden(true)
            form.hidden(false)
        }

        start.onClick {
            context?.setPreference(Preference.PAYMENT_AMOUNT, paymentViewModel.selectedAmount.value)
            context?.setPreference(Preference.PAYMENT_ISSUER, paymentViewModel.selectedBank.value?.issuerId)

            paymentViewModel.createPayment { payment ->
                val alert = android.app.AlertDialog.Builder(context, R.style.ParkeerAssistent_ContinueDialog)
                    .setMessage(R.string.payment_redirectMsg)
                    .setNegativeButton(R.string.common_continue) { _, _ ->
                        loadFragment<LoadingFragment>()
                        val redirect = Uri.parse(payment.redirectUrl)
                        val intent = Intent(Intent.ACTION_VIEW, redirect)
                        startActivity(intent)
                        paymentViewModel.transactionId = payment.transactionId
                    }
                    .create()
                alert.show()
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        paymentViewModel.getIdeal()
        if (! handler.hasCallbacks(checkStatus)) {
            handler.post(checkStatus)
        }
    }

    override fun onStop() {
        super.onStop()
        if (handler.hasCallbacks(checkStatus)) {
            handler.removeCallbacks(checkStatus)
            dialog.dismiss()
        }
    }

    private val checkStatus: Runnable = object : Runnable {
        override fun run() {
            paymentViewModel.checkStatus {
                success = {
                    paymentViewModel.transactionId = null
                    dialog.dismiss()
                    messageViewModel.success(getString(R.string.payment_successMsg))
                    userViewModel.getBalance()
                    loadFragment<UserFragment>()
                }
                pending = {
                    dialog.show()
                }
                error = {
                    paymentViewModel.transactionId = null
                    dialog.dismiss()
                    messageViewModel.error(getString(R.string.payment_errorMsg))
                }
                unknown = {
                    paymentViewModel.transactionId = null
                    dialog.dismiss()
                    messageViewModel.warn(getString(R.string.payment_unknownMsg))
                }
            }
            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(15))
        }
    }

    override fun back() {
        loadFragment<UserFragment>()
    }

    override fun hideMenuItem(menuItemId: Int): Boolean {
        return R.id.payment == menuItemId
    }

}