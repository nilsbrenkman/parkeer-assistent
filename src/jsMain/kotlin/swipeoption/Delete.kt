package swipeoption

import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import nl.parkeerassistent.style.Colors
import nl.parkeerassistent.style.MainStyles
import org.w3c.dom.events.Event
import react.RProps
import react.functionalComponent
import styled.css
import styled.styledDiv
import styled.styledI

external interface DeleteProps : RProps {
    var size: Size
    var onClick: (event: Event) -> Unit
}

val Delete = functionalComponent<DeleteProps> { props ->

    styledDiv {
        css {
            backgroundColor = hex(0xdc3545)
            cursor = Cursor.pointer
            height = props.size.height.px
            width = props.size.width.px
        }
        attrs.onClickFunction = props.onClick
        styledI {
            css {
                classes = mutableListOf("bi-trash")
                color = Colors.white
                +MainStyles.icon
            }
        }
    }

}
