package nl.parkeerassistent.android.ui.payment

import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.parkeerassistent.android.data.Issuer
import nl.parkeerassistent.android.service.PaymentService
import nl.parkeerassistent.android.service.model.IdealResponse
import nl.parkeerassistent.android.service.model.PaymentResponse
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    val paymentService: PaymentService,
) : ViewModel() {

    private val _ideal = MutableLiveData<IdealResponse>()
    val ideal: LiveData<IdealResponse> = _ideal

    var transactionId: String? = null

    val selectedAmount = SpinnerSelectListener<String>()
    val selectedBank = SpinnerSelectListener<Issuer>()

    fun getIdeal() {
        viewModelScope.launch {
            if (_ideal.value == null) {
                val response = withContext(Dispatchers.IO) {
                    paymentService.getIdeal()
                }
                _ideal.value = response
            }
        }
    }

    fun createPayment(callback: (PaymentResponse) -> Unit) {
        viewModelScope.launch {
            val amount = selectedAmount.value ?: return@launch
            val issuer = selectedBank.value?.issuerId ?: return@launch
            val response = withContext(Dispatchers.IO) {
                paymentService.createPayment(amount, issuer)
            }
            callback(response)
        }
    }

    fun checkStatus(builder: CheckStatusHandler.() -> Unit) {
        viewModelScope.launch {
            val t = transactionId ?: return@launch
            val handler = object : CheckStatusHandler() {}.apply(builder)
            val response = withContext(Dispatchers.IO) {
                paymentService.getStatus(t)
            }
            when (response.status) {
                "success" -> handler.success()
                "pending" -> handler.pending()
                "error"   -> handler.error()
                else      -> handler.unknown()
            }
        }
    }

    class SpinnerSelectListener<T> : AdapterView.OnItemSelectedListener {
        var value: T? = null
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            parent?.getItemAtPosition(pos)?.let { item ->
                @Suppress("UNCHECKED_CAST")
                value = item as T
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {
            value = null
        }
    }

    abstract class CheckStatusHandler {
        lateinit var success: () -> Unit
        lateinit var pending: () -> Unit
        lateinit var error: () -> Unit
        lateinit var unknown: () -> Unit
    }

}
