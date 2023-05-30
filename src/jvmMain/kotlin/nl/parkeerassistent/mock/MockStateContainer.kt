package nl.parkeerassistent.mock

import org.apache.log4j.Logger
import java.time.Instant

object MockStateContainer {

    val log = Logger.getLogger("MockStateContainer.kt")

    private var mockState = MockState()

    fun mock(): MockState {
        if (mockState.expiresAt < Instant.now()) {
            log.info("Mock expired")
            mockState = MockState()
        }
        return mockState
    }

    fun reset() {
        log.info("Resetting mock")
        mockState = MockState()
    }

}