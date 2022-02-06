package nl.parkeerassistent.android.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.parkeerassistent.android.BuildConfig
import nl.parkeerassistent.android.data.Issuer
import nl.parkeerassistent.android.service.model.IdealResponse
import nl.parkeerassistent.android.service.model.PaymentRequest
import nl.parkeerassistent.android.service.model.PaymentResponse
import nl.parkeerassistent.android.service.model.StatusResponse
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

interface PaymentService {

    suspend fun getIdeal(): IdealResponse
    suspend fun createPayment(amount: String, issuerId: String): PaymentResponse
    suspend fun getStatus(transactionId: String): StatusResponse

}

class PaymentClient(
    private val client: ApiClient
) : PaymentService {

    override suspend fun getIdeal(): IdealResponse {
        return client.get("payment")
    }

    override suspend fun createPayment(amount: String, issuerId: String): PaymentResponse {
        return client.post("payment", PaymentRequest(amount, issuerId))
    }

    override suspend fun getStatus(transactionId: String): StatusResponse {
        return client.get("payment/$transactionId")
    }

}

class PaymentMock : PaymentService {

    companion object {
        var paymentList: MutableMap<String, MockPayment> = HashMap()
    }

    override suspend fun getIdeal(): IdealResponse {
        ServiceUtil.mockDelay()

        return IdealResponse(
            listOf("5,00", "10,00", "15,00", "20,00", "30,00", "40,00", "50,00", "100,00"),
            listOf(Issuer("SUCCESS", "Success"),
                   Issuer("PENDING", "Pending"),
                   Issuer("PENDING10", "Pending 10s"),
                   Issuer("ERROR", "Error"))
        )
    }

    override suspend fun createPayment(amount: String, issuerId: String): PaymentResponse {
        ServiceUtil.mockDelay()

        val uuid = UUID.randomUUID().toString()
        val payment = MockPayment(uuid, issuerId, amount, System.currentTimeMillis())
        paymentList[uuid] = payment

        return PaymentResponse(BuildConfig.BASE_URL + "open", uuid)
    }

    override suspend fun getStatus(transactionId: String): StatusResponse {
        ServiceUtil.mockDelay()

        val payment = paymentList[transactionId] ?: return StatusResponse("error")
        return StatusResponse(payment.status)
    }

    data class MockPayment(val id: String, val issuer: String, val amount: String, val created: Long) {
        val status: String
            get() {
                return when (issuer) {
                    "SUCCESS" -> "success"
                    "PENDING" -> "pending"
                    "PENDING10" -> {
                        if (System.currentTimeMillis() - created > TimeUnit.SECONDS.toMillis(10)) {
                            return "success"
                        }
                        return "pending"
                    }
                    "ERROR" -> "pending"
                    else -> "unknown"
                }
            }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {
    @Singleton
    @Provides
    fun provide(client: ApiClient) : PaymentService {
        if (client.mock) {
            return PaymentMock()
        }
        return PaymentClient(client)
    }
}
