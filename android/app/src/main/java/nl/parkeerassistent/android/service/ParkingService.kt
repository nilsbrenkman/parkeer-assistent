package nl.parkeerassistent.android.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.parkeerassistent.android.data.HistoryResponse
import nl.parkeerassistent.android.data.Parking
import nl.parkeerassistent.android.data.ParkingResponse
import nl.parkeerassistent.android.data.Visitor
import nl.parkeerassistent.android.service.model.Response
import nl.parkeerassistent.android.service.model.StartParkingRequest
import nl.parkeerassistent.android.util.DateUtil.DateFormat.DateTime
import nl.parkeerassistent.android.util.addingMinutes
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

interface ParkingService {

    suspend fun getParking(): ParkingResponse
    suspend fun startParking(visitor: Visitor, timeMinutes: Int, start: Date?, regimeTimeEnd: String): Response
    suspend fun stopParking(parking: Parking): Response
    suspend fun getHistory(): HistoryResponse

}

class ParkingClient(
    private val client: ApiClient
) : ParkingService {

    override suspend fun getParking(): ParkingResponse {
        return client.get("parking")
    }

    override suspend fun startParking(
        visitor: Visitor,
        timeMinutes: Int,
        start: Date?,
        regimeTimeEnd: String
    ): Response {
        val startTime = start?.let { DateTime.format(start) }
        return client.post("parking", StartParkingRequest(visitor, timeMinutes, startTime, regimeTimeEnd))
    }

    override suspend fun stopParking(parking: Parking): Response {
        return client.delete("parking/${parking.id}")
    }

    override suspend fun getHistory(): HistoryResponse {
        return client.get("parking/history")
    }

}

class ParkingMock : ParkingService {

    companion object {
        var parkingList: MutableList<Parking> = ArrayList()
        private var nextId = 0
        fun calculateCost(minutes: Int): Double {
            return minutes.toDouble() * UserMock.HOUR_RATE / 60.0
        }
        fun start(visitor: Visitor, minutes: Int, start: Date) {
            val startTime = DateTime.format(start)
            val endTime = DateTime.format(start.addingMinutes(minutes))
            val cost = calculateCost(minutes)
            parkingList.add(Parking(nextId++, visitor.license, visitor.name, startTime, endTime, cost))
        }
    }

    init {
        start(VisitorMock.visitors[0], 15, Date().addingMinutes(-14))
        start(VisitorMock.visitors[1], 60, Date().addingMinutes(2))
        start(VisitorMock.visitors[0], 60, Date().addingMinutes(-24 * 60))
        start(VisitorMock.visitors[1], 300, Date().addingMinutes(-7 * 24 * 60))
        start(VisitorMock.visitors[0], 60, Date().addingMinutes(-31 * 24 * 60))
    }

    override suspend fun getParking(): ParkingResponse {
        ServiceUtil.mockDelay()

        val active = parkingList
            .filter { p -> p.startDate < Date() && p.endDate > Date() }
            .sortedBy { p -> p.endDate }
        val scheduled = parkingList
            .filter { p -> p.startDate > Date() }
            .sortedBy { p -> p.startDate }
        return ParkingResponse(active, scheduled)
    }

    override suspend fun startParking(
        visitor: Visitor,
        timeMinutes: Int,
        start: Date?,
        regimeTimeEnd: String
    ): Response {
        ServiceUtil.mockDelay()

        val startDate = start ?: Date()

        val startTime = DateTime.format(startDate)
        val endTime = DateTime.format(startDate.addingMinutes(timeMinutes))

        val cost = calculateCost(timeMinutes)
        if (cost > UserMock.getBalance()) {
            return Response(false, "Not enough balance")
        }

        parkingList.add(Parking(nextId++, visitor.license, visitor.name, startTime, endTime, cost))
        return Response(true)
    }

    override suspend fun stopParking(parking: Parking): Response {
        ServiceUtil.mockDelay()

        val stop = parkingList.find { p -> p.id == parking.id } ?: return Response(false, "Parking not found")

        val start = stop.startDate
        val end = Date()
        if (start > end) {
            parkingList.remove(stop)
            return Response(true)
        }

        val diff = end.time - start.time
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        stop.cost = calculateCost(minutes.toInt())
        stop.endTime = DateTime.format(end)
        return Response(true)
    }

    override suspend fun getHistory(): HistoryResponse {
        ServiceUtil.mockDelay()

        val history = parkingList
            .filter { p -> p.endDate < Date() }
            .sortedByDescending { p -> p.startDate }
        return HistoryResponse(history)
    }

}

@Module
@InstallIn(SingletonComponent::class)
object ParkingModule {
    @Singleton
    @Provides
    fun provide(client: ApiClient) : ParkingService {
        if (client.mock) {
            return ParkingMock()
        }
        return ParkingClient(client)
    }
}
