package nl.parkeerassistent

import io.ktor.client.request.delete
import io.ktor.client.request.url
import kotlinext.js.jsObject
import kotlinx.coroutines.launch
import kotlinx.css.Display
import kotlinx.css.Position
import kotlinx.css.VerticalAlign
import kotlinx.css.bottom
import kotlinx.css.display
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.lineHeight
import kotlinx.css.marginRight
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.properties.LineHeight
import kotlinx.css.px
import kotlinx.css.right
import kotlinx.css.verticalAlign
import nl.parkeerassistent.message.Message
import nl.parkeerassistent.message.messageBroker
import nl.parkeerassistent.model.Parking
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.model.Visitor
import nl.parkeerassistent.style.Elements
import nl.parkeerassistent.style.MainStyles
import react.RProps
import react.child
import react.dom.div
import react.functionalComponent
import react.useRef
import scope
import styled.css
import styled.styledDiv
import styled.styledSpan
import swipeoption.Delete
import swipeoption.DeleteProps
import swipeoption.SwipeOption
import swipeoption.SwipeOptionProps

external interface ParkingListProps : RProps {
    var parkingList: List<Parking>
    var visitorList: List<Visitor>
    var onChange: () -> Unit
}

val ParkingList = functionalComponent<ParkingListProps> { props ->

    fun stopParking(parking: Parking) {
        spinner.start()
        scope.launch {
            val result = client.delete<Response> {
                url(api("parking/${parking.id}"))
            }
            if (result.success) {
                messageBroker.show(Message("Sessie verwijderd", Message.MessageType.INFO))
                props.onChange()
            } else {
                if (result.message != null) {
                    messageBroker.show(Message(result.message, Message.MessageType.ERROR))
                } else {
                    messageBroker.show(Message("Onbekende fout", Message.MessageType.ERROR))
                }
            }
        }
    }

    val resetFun = useRef<(() -> Unit)?>(null)

    val doReset = fun() {
        resetFun.current?.invoke()
    }

    val setReset = fun(r: () -> Unit) {
        resetFun.current = r
    }

    child(Elements.Separator) {}

    for (parking in props.parkingList) {
        val visitor = props.visitorList.find{it.license==parking.license}
        val formattedLicense = visitor?.formattedLicense ?: License.format(parking.license)
        child(SwipeOption, props = jsObject<SwipeOptionProps> {
            left = arrayOf({
                child(Delete, props = jsObject<DeleteProps> {
                    size = it
                    onClick = {
                        doReset.invoke()
                        stopParking(parking)
                    }
                }) {}
            })
            right = emptyArray()
            size = jsObject {
                height = 60
                width = 60
            }
            snap = 0.7
            reset = doReset
            onReset = setReset
            showOptionsOnClick = "left"
        }) {
            styledDiv {
                css {
                    height = 100.pct
                    marginRight = MainStyles.marginHorizontal
                    verticalAlign = VerticalAlign.top
                    +MainStyles.license
                }
                +formattedLicense
            }
            styledDiv {
                css {
                    display = Display.inlineBlock
                }
                styledDiv {
                    css {
                        fontSize = 14.px
                        lineHeight = LineHeight("14px")
                    }
                    +Util.getTimeRange(parking)
                }
                div {
                    styledSpan {
                        css {
                            +MainStyles.visitorName
                        }
                        +visitor?.name.orEmpty()
                    }
                    styledSpan {
                        css {
                            position = Position.absolute
                            right = MainStyles.marginHorizontal
                            bottom = MainStyles.marginVertical
                            lineHeight = LineHeight("14px")
                        }
                        +"€ ${parking.cost}"
                    }
                }
            }

            child(Elements.Separator) {}

        }
    }

    child(Elements.Spacer) {}

}
