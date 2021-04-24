package swipeoption

import EventData
import Swipeable
import kotlinx.css.*
import kotlinx.css.properties.cubicBezier
import kotlinx.css.properties.s
import kotlinx.css.properties.transition
import kotlinx.css.properties.translateX
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onTouchCancelFunction
import kotlinx.html.js.onTouchEndFunction
import kotlinx.html.js.onTouchStartFunction
import nl.parkeerassistent.style.Colors
import nl.parkeerassistent.style.MainStyles
import org.w3c.dom.events.Event
import react.*
import styled.css
import styled.styledDiv

external interface Size {
    var height: Int
    var width: Int
}

external interface SwipeOptionProps : RProps {
    var left: Array<(size: Size) -> ReactElement>
    var right: Array<(size: Size) -> ReactElement>
    var size: Size
    var snap: Double
    var reset: () -> Unit
    var onReset: (() -> Unit) -> Unit
    var onClick: ((event: Event) -> Unit)?
    var showOptionsOnClick: String?
}

val SwipeOption = functionalComponent<SwipeOptionProps> { props ->

    val (pos, setPos) = useState(0)
    val (swiping, setSwiping) = useState(false)
    val (touch, setTouch) = useState(false)

    val limitLeft = 0 - (props.right.size * props.size.width)
    val limitRight = props.left.size * props.size.width

    fun getP(p: Int) : Int {
        if (p > limitRight) return limitRight
        if (p < limitLeft) return limitLeft
        return p
    }

    fun getP(e: EventData): Int {
        if (e.velocity.toDouble() > 2) {
            if (e.dir == "Left") {
                if (pos > 0) {
                    return limitRight
                }
                return 0
            }
            if (e.dir == "Right") {
                if (pos < 0) {
                    return limitLeft
                }
                return 0
            }
        }
        if (pos > limitRight * props.snap) {
            return limitRight
        }
        if (pos < limitLeft * props.snap) {
            return limitLeft
        }
        return 0
    }

    val onSwiping = fun(e: EventData) {
        if (e.dir == "Left" || e.dir == "Right") {
            if (! swiping) {
                props.reset()
                setSwiping(true)
            }
            val p = (pos - (e.deltaX.toInt() * 0.1) * 0.75).toInt()
            setPos(getP(p))
        }
    }

    val onSwiped = fun(e: EventData) {
        val p = getP(e)
        if (p != 0) {
            props.onReset {
                setPos(0)
            }
        }
        setPos(getP(e))
    }

    Swipeable {
        attrs.delta = 10
        attrs.trackMouse = true
        attrs.trackTouch = true
        attrs.onSwiping = onSwiping
        attrs.onSwiped = onSwiped

        styledDiv {
            css {
                position = Position.relative
                height = props.size.height.px
                overflowX = Overflow.hidden
            }
            if (props.left.isNotEmpty()) {
                styledDiv {
                    css {
                        position = Position.absolute
                        left = 0.px
                    }
                    for (e in props.left) {
                        childList.add(e.invoke(props.size))
                    }
                }
            }
            if (props.right.isNotEmpty()) {
                styledDiv {
                    css {
                        position = Position.absolute
                        right = 0.px
                    }
                    for (e in props.right) {
                        childList.add(e.invoke(props.size))
                    }
                }
            }
            styledDiv {
                css {
                    height = props.size.height.px
                    +MainStyles.padding
                    cursor = Cursor.pointer
                    backgroundColor = if (touch) Colors.lightestGrey else Colors.white
                    transform.translateX(pos.px)
                    transition("transform", 0.6.s, cubicBezier(0.23, 1.0, 0.32, 1.0))
                    hover {
                        backgroundColor = Colors.lightestGrey
                    }
                }
                attrs.onClickFunction = {
                    if (swiping) {
                        setSwiping(false)
                    } else if (pos != 0) {
                        setPos(0)
                    } else {
                        if (props.onClick != null) {
                            props.onClick?.invoke(it)
                        } else {
                            if ("left" == props.showOptionsOnClick) {
                                setPos(limitRight)
                            }
                            if ("right" == props.showOptionsOnClick) {
                                setPos(limitLeft)
                            }
                        }
                    }
                }
                attrs.onTouchStartFunction = { setTouch(true) }
                attrs.onTouchCancelFunction = { setTouch(false) }
                attrs.onTouchEndFunction = { setTouch(false) }
                props.children()
            }
        }
    }
}
