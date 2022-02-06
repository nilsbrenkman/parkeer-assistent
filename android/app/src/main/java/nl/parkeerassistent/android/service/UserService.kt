package nl.parkeerassistent.android.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.parkeerassistent.android.service.model.BalanceResponse
import nl.parkeerassistent.android.service.model.RegimeResponse
import nl.parkeerassistent.android.service.model.UserResponse
import nl.parkeerassistent.android.util.DateUtil
import nl.parkeerassistent.android.util.DateUtil.DateFormat
import nl.parkeerassistent.android.util.getDateWithHour
import nl.parkeerassistent.android.util.isWeekend
import java.util.*
import javax.inject.Singleton

interface UserService {

    suspend fun getUser(): UserResponse
    suspend fun getBalance(): BalanceResponse
    suspend fun getRegime(date: Date): RegimeResponse

}

class UserClient(
    private val client: ApiClient
) : UserService {

    override suspend fun getUser(): UserResponse {
        return client.get("user")
    }

    override suspend fun getBalance(): BalanceResponse {
        return client.get("user/balance")
    }

    override suspend fun getRegime(date: Date): RegimeResponse {
        return client.get("user/regime/${DateFormat.DateFull.format(date)}")
    }

}

class UserMock : UserService {

    companion object {
        const val HOUR_RATE: Double = 2.1
        fun getBalance(): Double {
            var balance = 20.0
            balance -= ParkingMock.parkingList.sumOf { p -> p.cost }
            balance += PaymentMock.paymentList.values.filter { p -> p.status == "success" }.sumOf { p -> p.amount.replace(",", ".").toDouble() }
            return balance
        }
    }

    override suspend fun getUser(): UserResponse {
        ServiceUtil.mockDelay()

        val date = Date()
        return UserResponse(formatBalance(UserMock.getBalance()), HOUR_RATE, getRegimeStart(date), getRegimeEnd(date))
    }

    override suspend fun getBalance(): BalanceResponse {
        ServiceUtil.mockDelay()

        return BalanceResponse(formatBalance(UserMock.getBalance()))
    }

    override suspend fun getRegime(date: Date): RegimeResponse {
        ServiceUtil.mockDelay()

        return RegimeResponse(getRegimeStart(date), getRegimeEnd(date))
    }

    private fun formatBalance(balance: Double): String {
        return "%.2f".format(Locale.ENGLISH, balance)
    }

    private fun getRegimeStart(date: Date): String {
        val calendar = Calendar.getInstance(DateUtil.amsterdam)
        calendar.time = date
        if (calendar.isWeekend()) {
            return DateFormat.DateTime.format(calendar.getDateWithHour(12))
        }
        return DateFormat.DateTime.format(calendar.getDateWithHour(9))
    }

    private fun getRegimeEnd(date: Date): String {
        val calendar = Calendar.getInstance(DateUtil.amsterdam)
        calendar.time = date
        if (calendar.isWeekend()) {
            return DateFormat.DateTime.format(calendar.getDateWithHour(21))
        }
        calendar.add(Calendar.DATE, 1)
        return DateFormat.DateTime.format(calendar.getDateWithHour(0))
    }

}

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    @Singleton
    @Provides
    fun provide(client: ApiClient) : UserService {
        if (client.mock) {
            return UserMock()
        }
        return UserClient(client)
    }
}
