@file:JsModule("react-bootstrap")
@file:JsNonModule

import org.w3c.dom.events.Event
import react.RClass
import react.RProps

@JsName("Button")
external val Button: RClass<ButtonProps>

external interface ButtonProps : RProps {
    var variant: String
    var onClick: (event: Event) -> Unit
    var block: Boolean
}
