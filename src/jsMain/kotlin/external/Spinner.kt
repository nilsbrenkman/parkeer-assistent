@file:JsModule("react-bootstrap")
@file:JsNonModule

import react.RClass
import react.RProps

@JsName("Spinner")
external val Spinner: RClass<SpinnerProps>

external interface SpinnerProps : RProps {
    var animation: String
}
