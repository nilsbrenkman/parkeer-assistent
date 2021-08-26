package nl.parkeerassistent.service

class ServiceException(val type: Type, message: String) : Exception(message) {

    enum class Type {
        API,
        SCREENSCRAPING,
        ;
    }

}

