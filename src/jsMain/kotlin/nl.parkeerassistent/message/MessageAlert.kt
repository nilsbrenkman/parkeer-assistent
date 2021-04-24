package nl.parkeerassistent.message

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.css.properties.Timing
import kotlinx.css.properties.s
import kotlinx.css.properties.transition
import nl.parkeerassistent.style.MainStyles
import react.RProps
import react.functionalComponent
import react.useEffect
import react.useState
import scope
import styled.css
import styled.styledDiv

external interface MessageAlertProps : RProps {
    var message: Message
}

var MessageAlert = functionalComponent<MessageAlertProps> { props ->

    val (hide, setHide) = useState(false)
    val (visible, setVisible) = useState(true)

    useEffect {
        scope.launch {
            delay(3 * 1000L)
            messageBroker.hide {
                setHide(true)
                delay(1000L)
                setVisible(false)
            }
        }
    }

    if (visible) {
        styledDiv {
            css {
                position = Position.relative
                marginTop = if (hide) (-48).px else 6.px
                transition("margin", 0.6.s, Timing.linear)
                color = props.message.type.color
                backgroundColor = props.message.type.backgroundColor
                +MainStyles.padding
            }
            +props.message.message
        }
    }

}