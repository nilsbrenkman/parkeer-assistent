package nl.parkeerassistent

class ServiceException(val type: Type, message: String) : Exception(message) {

    enum class Type {
        API,
        SCREENSCRAPING,
        ;
    }

}

