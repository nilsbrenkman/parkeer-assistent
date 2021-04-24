package nl.parkeerassistent.message

import kotlinx.css.Color

data class Message(
    val message: String,
    val type: MessageType
) {

    enum class MessageType(val color: Color, val backgroundColor: Color) {
        INFO   (Color("#084298"), Color("#cfe2ff")),
        SUCCESS(Color("#0f5132"), Color("#d1e7dd")),
        WARN   (Color("#664d03"), Color("#fff3cd")),
        ERROR  (Color("#842029"), Color("#f8d7da")),
        ;
    }

}

