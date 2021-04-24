package nl.parkeerassistent.style

enum class ZIndex {

    Header,
    Messages,
    Spinner,
    SpinnerContainer,
    InfoClose,
    Info,
    InfoContainer,
    ;

    fun get(): Int {
        return values().size - ordinal + 10
    }

}