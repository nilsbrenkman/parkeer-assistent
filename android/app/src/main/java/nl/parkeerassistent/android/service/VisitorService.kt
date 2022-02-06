package nl.parkeerassistent.android.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.parkeerassistent.android.data.Visitor
import nl.parkeerassistent.android.service.model.AddVisitorRequest
import nl.parkeerassistent.android.service.model.Response
import nl.parkeerassistent.android.service.model.VisitorResponse
import javax.inject.Singleton

interface VisitorService {

    suspend fun getVisitors(): VisitorResponse
    suspend fun addVisitor(license: String, name: String): Response
    suspend fun deleteVisitor(visitor: Visitor): Response

}

class VisitorClient(
    private val client: ApiClient
) : VisitorService {

    override suspend fun getVisitors(): VisitorResponse {
        return client.get("visitor")
    }

    override suspend fun addVisitor(license: String, name: String): Response {
        return client.post("visitor", AddVisitorRequest(license, name))
    }

    override suspend fun deleteVisitor(visitor: Visitor): Response {
        return client.delete("visitor/${visitor.visitorId}")
    }

}

class VisitorMock : VisitorService {

    companion object {
        var visitors: MutableList<Visitor> = ArrayList()
        private var nextId = 0
        init {
            MockVisitor.values().forEach { v -> add(v.license, v.name) }
        }
        fun add(license: String, name: String) {
            visitors.add(Visitor(nextId++, 999, license, license, name))
        }
    }

    override suspend fun getVisitors(): VisitorResponse {
        ServiceUtil.mockDelay()

        return VisitorResponse(ArrayList(visitors))
    }

    override suspend fun addVisitor(license: String, name: String): Response {
        ServiceUtil.mockDelay()

        if (visitors.size > 8) {
            return Response(false, "Too many visitors")
        }
        add(license, name)
        return Response(true)
    }

    override suspend fun deleteVisitor(visitor: Visitor): Response {
        ServiceUtil.mockDelay()

        val removed = visitors.removeIf { v -> v.visitorId == visitor.visitorId }
        return Response(removed)
    }

    enum class MockVisitor(val license: String) {
        Suzanne("111-AA-1"),
        Erik   ("22-BBB-2"),
    }

}

@Module
@InstallIn(SingletonComponent::class)
object VisitorModule {
    @Singleton
    @Provides
    fun provide(client: ApiClient) : VisitorService {
        if (client.mock) {
            return VisitorMock()
        }
        return VisitorClient(client)
    }
}
