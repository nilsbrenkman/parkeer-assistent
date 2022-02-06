package nl.parkeerassistent.android.data

data class Message(
    val message: String,
    val level: Level
)

enum class Level {
    INFO,
    SUCCESS,
    WARN,
    ERROR,
    ;
}