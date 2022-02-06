package nl.parkeerassistent.android.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.parkeerassistent.android.service.ParkingService
import nl.parkeerassistent.android.service.UserService
import javax.inject.Inject

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class ParkingRepository @Inject constructor(
    val state: State,
    val parkingService: ParkingService,
    val userService: UserService,
) {

    suspend fun getParking(): ParkingResponse {
        return state.parking ?: withContext(Dispatchers.IO) {
            val response = parkingService.getParking()
            val withName = ParkingResponse(addNameToParking(response.active, state.visitors),
                                           addNameToParking(response.scheduled, state.visitors))
            state.parking = withName
            withName
        }
    }

    suspend fun getHistory(): HistoryResponse {
        return state.history ?: withContext(Dispatchers.IO) {
            val response = parkingService.getHistory()
            val withName = HistoryResponse(addNameToParking(response.history, state.visitors))
            state.history = withName
            withName
        }
    }

    private fun addNameToParking(parking: List<Parking>, visitors: List<Visitor>?): List<Parking> {
        visitors ?: return parking
        val addName = fun(p: Parking): Parking {
            val v = visitors.firstOrNull { v -> v.license == p.license || v.formattedLicense == p.license }
            v?.let {
                return p.copy(name = v.name)
            }
            return p
        }
        return parking.map(addName)
    }

}