@file:JsModule("react-swipeable")
@file:JsNonModule

import react.RClass
import react.RProps

@JsName("Swipeable")
external val Swipeable: RClass<SwipeableProps>

external interface SwipeableProps : RProps {
    var delta: Int
    var trackTouch: Boolean
    var trackMouse: Boolean
    var onSwiping: (eventData: EventData) -> Unit
    var onSwiped: (eventData: EventData) -> Unit
}

external interface EventData {
    var event: dynamic /* MouseEvent | TouchEvent */
        get() = definedExternally
        set(value) = definedExternally
    var deltaX: Number
    var deltaY: Number
    var absX: Number
    var absY: Number
    var first: Boolean
    var initial: dynamic /* JsTuple<Number, Number> */
        get() = definedExternally
        set(value) = definedExternally
    var velocity: Number
    var dir: String /* "Left" | "Right" | "Up" | "Down" */
}