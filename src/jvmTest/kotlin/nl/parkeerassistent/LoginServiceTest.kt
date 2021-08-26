package nl.parkeerassistent

import nl.parkeerassistent.service.LoginService.findCustomerId
import org.junit.Assert
import org.junit.Test

class LoginServiceTest {

    @Test
    fun testFindCustomerId() {
        val customerId = findCustomerId("customerDsh.init('{\\\"permits\\\":null,\\\"permitList\\\":[{\\\"id\\\":262621,\\\"customerId\\\":258651,\\\"permitMediaCode\\\":\\\"100365\\\",")
        Assert.assertEquals("258651", customerId)
    }

}