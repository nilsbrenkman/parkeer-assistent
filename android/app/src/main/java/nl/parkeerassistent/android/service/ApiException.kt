package nl.parkeerassistent.android.service

import java.io.IOException

abstract class ApiException(message: String) : Exception(message) {
    abstract fun getErrorMessage(): String
}

class NoContentException : ApiException("No content") {
    override fun getErrorMessage(): String = "No response data"
}

class ServiceException(val code: Int, message: String) : ApiException(message) {
    override fun getErrorMessage(): String = "Server error: ${code} [${message}]"
}

class NetworkException(private val exception: IOException) : ApiException(exception.message ?: "IOException") {
    override fun getErrorMessage(): String = "Network error: ${exception.message}"
}
