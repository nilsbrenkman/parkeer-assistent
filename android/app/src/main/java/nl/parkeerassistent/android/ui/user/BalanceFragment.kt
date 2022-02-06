package nl.parkeerassistent.android.ui.user

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.ui.BaseFragment
import nl.parkeerassistent.android.util.hidden

@AndroidEntryPoint
class BalanceFragment : BaseFragment() {

    override fun createFragment(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bundle: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_balance, container, false)

        val loading = view.findViewById<ProgressBar>(R.id.loading)
        val balance = view.findViewById<TextView>(R.id.balance)

        userViewModel.balance.observe(viewLifecycleOwner) { b ->
            balance.text = formatBalance(b)
            loading.hidden(true)
            balance.hidden(false)
        }

        return view
    }

    private fun formatBalance(balance: String): SpannableStringBuilder {
        val prefix = "${getString(R.string.user_balance)}: "
        val bold = "€ $balance"
        val builder = SpannableStringBuilder("$prefix$bold")
        builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD), prefix.length, prefix.length + bold.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }

}