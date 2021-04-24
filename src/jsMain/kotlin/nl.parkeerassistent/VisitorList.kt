package nl.parkeerassistent

import Button
import io.ktor.client.request.*
import kotlinext.js.jsObject
import kotlinx.coroutines.launch
import kotlinx.css.height
import kotlinx.css.marginRight
import kotlinx.css.pct
import nl.parkeerassistent.message.Message
import nl.parkeerassistent.message.messageBroker
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.model.Visitor
import nl.parkeerassistent.style.Elements
import nl.parkeerassistent.style.MainStyles
import react.RProps
import react.child
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

external interface VisitorListProps : RProps {
    var visitorList: List<Visitor>
    var selectVisitor: (Visitor) -> Unit
    var add: () -> Unit
    var delete: () -> Unit
}

val VisitorList = functionalComponent<VisitorListProps> { props ->

    val resetFun = useRef<(() -> Unit)?>(null)

    fun deleteVisitor(visitor: Visitor) {
        spinner.start()
        scope.launch {
            val result = client.delete<Response>(api("visitor/${visitor.visitorId}"))
            if (result.success) {
                messageBroker.show(Message("Bezoeker verwijderd", Message.MessageType.INFO))
                props.delete()
            } else {
                if (result.message != null) {
                    messageBroker.show(Message(result.message, Message.MessageType.WARN))
                } else {
                    messageBroker.show(Message("Onbekende fout", Message.MessageType.ERROR))
                }
            }
        }
    }

    val doReset = fun() {
        resetFun.current?.invoke()
    }

    val setReset = fun(r: () -> Unit) {
        resetFun.current = r
    }

    child(Elements.Separator) {}

    for (visitor in props.visitorList) {
        child(SwipeOption, props = jsObject<SwipeOptionProps> {
            left = arrayOf({
                child(Delete, props = jsObject<DeleteProps> {
                    size = it
                    onClick = {
                        doReset.invoke()
                        deleteVisitor(visitor)
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
            onClick = {
                props.selectVisitor(visitor)
            }
        }) {
            styledDiv {
                css {
                    height = 100.pct
                    marginRight = MainStyles.marginHorizontal
                    +MainStyles.license
                }
                +visitor.formattedLicense
            }
            styledSpan {
                css {
                    +MainStyles.visitorName
                }
                +visitor.name
            }
        }

        child(Elements.Separator) {}

    }

    child(Elements.Spacer) {}

    Button {
        attrs.variant = "success"
        attrs.onClick = {
            props.add()
        }
        attrs.block = true
        +"Nieuwe bezoeker"
    }

}

