package nl.parkeerassistent.message

import kotlin.jvm.Synchronized

class MessageBroker {

    private var addMessage: ((Message) -> Unit)? = null
    private var hiding: Boolean = false
    private var queue: ArrayDeque<suspend () -> Unit> = ArrayDeque()

    fun register(addMessage: (Message) -> Unit) {
        this.addMessage = addMessage
    }

    fun show(message: Message) {
        addMessage?.invoke(message)
    }

    @Synchronized
    suspend fun hide(hide: suspend () -> Unit) {
        if (hiding) {
            queue.add(hide)
            return
        }
        hiding = true
        hide.invoke()
        hiding = false
        if (queue.isNotEmpty()) {
            hide(queue.removeFirst())
        }
    }

}

