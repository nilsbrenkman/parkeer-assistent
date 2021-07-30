package nl.parkeerassistent

import Button
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinext.js.jsObject
import kotlinx.coroutines.launch
import kotlinx.css.*
import nl.parkeerassistent.message.Message
import nl.parkeerassistent.message.messageBroker
import nl.parkeerassistent.model.AddParkingRequest
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.model.Visitor
import nl.parkeerassistent.style.Elements
import nl.parkeerassistent.style.MainStyles
import react.RProps
import react.child
import react.dom.div
import react.functionalComponent
import react.useState
import scope
import styled.css
import styled.styledDiv
import kotlin.js.Date
import kotlin.math.roundToInt

interface TimeButtonsProps : RProps {
    var minutes: Int
    var onClick: (minutes: Int) -> Unit
}

val TimeButtons = functionalComponent<TimeButtonsProps> { props ->
    styledDiv {
        css {
            +MainStyles.padding
            button {
                width = 47.5.pct
                adjacentSibling("button") {
                    marginLeft = 5.pct
                }
            }
        }
        Button {
            attrs.variant = "danger"
            attrs.onClick = {
                props.onClick(-props.minutes)
            }
            +"- ${props.minutes}"
        }
        Button {
            attrs.variant = "success"
            attrs.onClick = {
                props.onClick(props.minutes)
            }
            +"+ ${props.minutes}"
        }
    }
}

external interface AddParkingProps : RProps {
    var selectedVisitor: Visitor
    var balance: String
    var hourRate: Double
    var regimeTimeEnd: String
    var onSuccess: () -> Unit
    var close: () -> Unit
}

val AddParking = functionalComponent<AddParkingProps> { props ->

    val (timeMinutes, setTimeMinutes) = useState(0)
    val timeBalance = (props.balance.replace(".", "").toInt() / (props.hourRate * 100) * 60).roundToInt()

    fun changeTime(minutes: Int) {
        var newVal = maxOf(timeMinutes + minutes, 0)
        if (newVal > timeBalance) {
            messageBroker.show(Message("Maximale parkeertijd vanwege saldo is $timeBalance minuten", Message.MessageType.WARN))
            newVal = timeBalance
        }
        val endDate = Date(Date().getTime() + newVal * 60000)
        val regimeEnd = Date(Date.parse(props.regimeTimeEnd))
        if (endDate.getTime() > regimeEnd.getTime()) {
            messageBroker.show(Message("Betaald parkeren tot ${Util.formatTime(regimeEnd)}", Message.MessageType.WARN))
            newVal = ((regimeEnd.getTime() - Date().getTime()) / 60000).toInt() + 1
        }
        setTimeMinutes(newVal)
    }

    fun start() {
        spinner.start()
        scope.launch {
            val result = client.post<Response> {
                url(api("parking"))
                contentType(ContentType.Application.Json)
                body = AddParkingRequest(
                    props.selectedVisitor,
                    timeMinutes,
                    props.regimeTimeEnd
                )
            }
            if (result.success) {
                messageBroker.show(Message("Nieuwe sessie gestart", Message.MessageType.SUCCESS))
                props.onSuccess()
            }
        }
    }

    styledDiv {
        css {
            position = Position.relative
            left = 50.pct
            marginTop = MainStyles.marginVertical
            marginLeft = (-MainStyles.licenseWidth / 2).px
            +MainStyles.license
        }
        +props.selectedVisitor.formattedLicense
    }
    styledDiv {
        css {
            textAlign = TextAlign.center
            +MainStyles.visitorName
            +MainStyles.padding
        }
        +props.selectedVisitor.name.orEmpty()
    }
    styledDiv {
        css {
            +MainStyles.padding
        }
        styledDiv {
            css {
                +MainStyles.third
            }
            +"Minuten:"
            div {
                +timeMinutes.toString()
            }
        }
        styledDiv {
            css {
                +MainStyles.third
            }
            +"Eindtijd:"
            div {
                val endDate = Date(Date().getTime() + timeMinutes * 60000)
                +Util.formatTime(endDate)
            }
        }
        styledDiv {
            css {
                +MainStyles.third
            }
            +"Kosten:"
            div {
                val cost = timeMinutes * props.hourRate / 60
                +"€ ${Util.formatAmount(cost)}"
            }
        }
    }

    div {
        child(TimeButtons, props = jsObject<TimeButtonsProps> {
            minutes = 1
            onClick = {
                changeTime(it)
            }
        }) {}
        child(TimeButtons, props = jsObject<TimeButtonsProps> {
            minutes = 10
            onClick = {
                changeTime(it)
            }
        }) {}
        child(TimeButtons, props = jsObject<TimeButtonsProps> {
            minutes = 60
            onClick = {
                changeTime(it)
            }
        }) {}
    }

    child(Elements.Spacer) {}

    Button {
        attrs.variant = "success"
        attrs.block = true
        attrs.onClick = {
            start()
        }
        +"Start"
    }
    Button {
        attrs.variant = "outline-danger"
        attrs.block = true
        attrs.onClick = {
            props.close()
        }
        +"Annuleren"
    }
}

