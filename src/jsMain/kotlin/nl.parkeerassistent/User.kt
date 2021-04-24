package nl.parkeerassistent

import io.ktor.client.request.*
import kotlinext.js.jsObject
import kotlinx.coroutines.launch
import kotlinx.css.*
import nl.parkeerassistent.model.*
import nl.parkeerassistent.style.Colors
import nl.parkeerassistent.style.Elements
import nl.parkeerassistent.style.MainStyles
import react.*
import react.dom.div
import react.dom.h3
import react.dom.h4
import scope
import styled.css
import styled.styledDiv
import styled.styledP

val User = functionalComponent<RProps> {

    val (visitors, setVisitors) = useState(emptyList<Visitor>())
    val (balance, setBalance) = useState("")
    val (hourRate, setHourRate) = useState(0.0)
    val (regimeTimeEnd, setRegimeTimeEnd) = useState("")
    val (active, setActive) = useState(emptyList<Parking>())
    val (scheduled, setScheduled) = useState(emptyList<Parking>())
    val (selectedVisitor, setSelectedVisitor) = useState<Visitor?>(null)
    val (addVisitor, setAddVisitor) = useState(false)

    fun getParking() {
        scope.launch {
            val result = client.get<ParkingResponse>(api("parking"))
            setActive(result.active)
            setScheduled(result.scheduled)
            spinner.stop()
        }
    }

    fun getBalance() {
        scope.launch {
            val result = client.get<BalanceResponse>(api("user/balance"))
            setBalance(result.balance)
        }
    }

    fun getVisitors() {
        scope.launch {
            val result = client.get<VisitorResponse>(api("visitor"))
            setVisitors(result.visitors)
            spinner.stop()
        }
    }

    fun getUser() {
        scope.launch {
            val result = client.get<UserResponse>(api("user"))
            setBalance(result.balance)
            setHourRate(result.hourRate)
            setRegimeTimeEnd(result.regimeTimeEnd)
            getVisitors()
            getParking()
        }
    }

    fun onChange() {
        getBalance()
        getParking()
    }

    useEffect(dependencies = listOf()) {
        scope.launch {
            getUser()
        }
    }

    fun RBuilder.parkingList(list: List<Parking>): ReactElement {
        return child(ParkingList, props = jsObject<ParkingListProps> {
            visitorList = visitors
            parkingList = list
            onChange = {
                onChange()
            }
        }) {}
    }

    styledDiv {
        css {
            +MainStyles.balance
        }
        +"Saldo: € $balance"
    }
    styledDiv {
        css {
            height = 29.px
        }
    }

    if (selectedVisitor != null) {
        child(AddParking, props = jsObject<AddParkingProps> {
            this.selectedVisitor = selectedVisitor
            this.balance = balance
            this.hourRate = hourRate
            this.regimeTimeEnd = regimeTimeEnd
            onSuccess = {
                onChange()
                setSelectedVisitor(null)
            }
            close = {
                setSelectedVisitor(null)
            }
        }) {}
    } else if (addVisitor) {
        child(AddVisitor, props = jsObject<AddVisitorProps> {
            onSuccess = { visitors: List<Visitor>, selected: Visitor ->
                setVisitors(visitors)
                setSelectedVisitor(selected)
                setAddVisitor(false)
                spinner.stop()
            }
            close = {
                setAddVisitor(false)
            }
        }) {}
    } else {
        div {
            h3 {
                +"Parkeer sessies"
            }
            if (active.isEmpty() && scheduled.isEmpty()) {
                styledDiv {
                    css {
                        textAlign = TextAlign.center
                        fontSize = 18.px
                        fontStyle = FontStyle.italic
                        color = Colors.darkGrey
                        +MainStyles.padding
                    }
                    +"Geen actieve of geplande sessies"
                }

                child(Elements.Spacer) {}

            } else {
                if (active.isNotEmpty()) {
                    h4 {
                        +"Actieve sessies"
                    }
                    parkingList(active)
                }
                if (scheduled.isNotEmpty()) {
                    h4 {
                        +"Geplande sessies"
                    }
                    parkingList(scheduled)
                }
            }
        }

        div {
            h3 {
                +"Bezoekers"
            }
            child(VisitorList, props = jsObject<VisitorListProps> {
                visitorList = visitors
                selectVisitor = setSelectedVisitor
                add = { setAddVisitor(true) }
                delete = { getVisitors() }
            }) {}
        }
    }

}
