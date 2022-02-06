package nl.parkeerassistent.android.ui.parking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.parkeerassistent.android.data.Parking
import nl.parkeerassistent.android.data.ParkingRepository
import nl.parkeerassistent.android.data.ParkingResponse
import nl.parkeerassistent.android.data.Regime
import nl.parkeerassistent.android.data.State
import nl.parkeerassistent.android.data.Visitor
import nl.parkeerassistent.android.service.Callback
import nl.parkeerassistent.android.util.DateUtil
import nl.parkeerassistent.android.util.DateUtil.DateFormat.DateTime
import nl.parkeerassistent.android.util.NotificationUtil
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ParkingViewModel @Inject constructor(
    val state: State,
    val parkingRepository: ParkingRepository,
    val notificationUtil: NotificationUtil,
) : ViewModel() {

    private val _parking = object : MutableLiveData<ParkingResponse>() {
        override fun setValue(value: ParkingResponse?) {
            super.setValue(value)
            _loading.value = false
        }
    }
    val parking: LiveData<ParkingResponse> = _parking

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _addParkingForm = MutableLiveData<AddParkingForm>()
    val addParkingForm: LiveData<AddParkingForm> = _addParkingForm

    private val _validationError = MutableLiveData<AddParkingForm.ValidationError?>()
    val validationError: LiveData<AddParkingForm.ValidationError?> = _validationError

    private val _history = MutableLiveData<List<Parking>?>()
    val history: LiveData<List<Parking>?> = _history

    fun getParking() {
        viewModelScope.launch {
            _parking.value = parkingRepository.getParking()
        }
    }

    fun startParking(visitor: Visitor, callback: Callback? = null) {
        viewModelScope.launch {
            _addParkingForm.value?.let { form ->
                val response = withContext(Dispatchers.IO) {
                    parkingRepository.parkingService.startParking(
                        visitor,
                        form.minutes,
                        form.start,
                        DateTime.format(form.regime.end)
                    )
                }
                if (response.success) {
                    parkingRepository.state.parking = null
                    _loading.value = true
                    _validationError.value = null
                }
                callback?.perform(response.success)
            }
        }
    }

    fun stopParking(parking: Parking, callback: Callback? = null) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                parkingRepository.parkingService.stopParking(parking)
            }
            if (response.success) {
                parkingRepository.state.parking = null
                _parking.value = parkingRepository.getParking()
            }
            callback?.perform(response.success)
        }
    }

    fun getHistory() {
//        if (refresh) {
//            parkingRepository.state.history = null
//        }
        _history.value = null
        viewModelScope.launch {
            _history.value = withContext(Dispatchers.IO) {
                parkingRepository.getHistory().history
            }
        }
    }

    fun startForm() {
        parkingRepository.state.user?.let { user ->
            _addParkingForm.value = AddParkingForm(user.balance.toDouble(), user.regime, user.hourRate).validate()
        }
    }

    fun updateForm() {
        update { form -> form.validate() }
    }

    fun setDate(year: Int, month: Int, day: Int) {
        update { form ->
            val c = Calendar.getInstance(DateUtil.amsterdam)
            c.time = form.start

            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month)
            c.set(Calendar.DAY_OF_MONTH, day)

            form.start = c.time

            viewModelScope.launch {
                val response = withContext(Dispatchers.IO) {
                    parkingRepository.userService.getRegime(c.time)
                }
                form.regime = Regime(response.regimeTimeStart, response.regimeTimeEnd)
                if (! form.fixedStart) {
                    form.start = form.regime.start
                }
                _addParkingForm.value = form.validate()
            }
            form
        }
    }

    fun setStart(hour: Int, minute: Int) {
        update(true) { form ->
            val c = Calendar.getInstance(DateUtil.amsterdam)
            c.time = form.start

            c.set(Calendar.HOUR_OF_DAY, hour)
            c.set(Calendar.MINUTE, minute)

            form.start = c.time
            form.fixedStart = true
            form.validate()
        }
    }

    fun setEnd(hour: Int, minute: Int) {
        update(true) { form ->
            val c = Calendar.getInstance(DateUtil.amsterdam)
            c.time = form.start

            c.set(Calendar.HOUR_OF_DAY, hour)
            c.set(Calendar.MINUTE, minute)

            form.end = c.time
            form.fixedEnd = true
            form.validate()
        }
    }

    fun setMinutes(minutes: Int) {
        update(true) { form ->
            form.minutes = minutes
            form.fixedEnd = false
            form.validate()
        }
    }

    private fun update(showValidationError: Boolean = false,
                       update: (AddParkingForm) -> AddParkingForm) {
        _addParkingForm.value?.let { form ->
            val updatedForm = update(form)
            _addParkingForm.value = updatedForm
            if (showValidationError && updatedForm.validationError != null) {
                _validationError.value = updatedForm.validationError
            }
        }
    }

}