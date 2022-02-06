package nl.parkeerassistent.android.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.parkeerassistent.android.service.model.Response
import javax.inject.Singleton

class State {

    var loggedIn: Response? = null
    var user: User? = null
    var visitors: List<Visitor>? = null
    var parking: ParkingResponse? = null
    var history: HistoryResponse? = null

    fun reset() {
        loggedIn = null
        user = null
        visitors = null
        parking = null
        history = null
    }

}

@Module
@InstallIn(SingletonComponent::class)
object StateModule {
    @Singleton
    @Provides
    fun provide() : State {
        return State()
    }
}
