package nl.parkeerassistent.message

import kotlinext.js.jsObject
import kotlinx.css.*
import nl.parkeerassistent.style.ZIndex
import react.*
import styled.css
import styled.styledDiv

val messageBroker = MessageBroker()

val MessageList = functionalComponent<RProps> {

    val messageList = useRef<ArrayDeque<ReactElement>>(ArrayDeque())
    val (messages, setMessages) = useState<List<ReactElement>>(emptyList())

    val addMessage = fun(message: Message) {
        val alert = child(MessageAlert, props = jsObject<MessageAlertProps> {
            this.message = message
        }) {}
        messageList.current.add(alert)
        setMessages(messageList.current.toList())
    }

    useEffect {
        messageBroker.register(addMessage)
    }

    styledDiv {
        css {
            position = Position.absolute
            width = 100.pct
            zIndex = ZIndex.Messages.get()
        }
        childList.addAll(messages)
    }

}
