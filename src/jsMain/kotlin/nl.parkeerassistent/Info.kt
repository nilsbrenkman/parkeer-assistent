package nl.parkeerassistent

import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.html.js.onClickFunction
import nl.parkeerassistent.model.Parking
import nl.parkeerassistent.model.Visitor
import nl.parkeerassistent.style.Colors
import nl.parkeerassistent.style.MainStyles
import nl.parkeerassistent.style.ZIndex
import org.w3c.dom.events.Event
import react.RProps
import react.dom.a
import react.dom.p
import react.functionalComponent
import styled.*

external interface InfoProps : RProps {
    var visible: Boolean
    var close: (event: Event) -> Unit
}

val Info = functionalComponent<InfoProps> { props ->

    styledDiv {
        css {
            display = if (props.visible) Display.block else Display.none
            position = Position.fixed
            top = 0.px
            width = 100.pct
            height = 100.pct
            backgroundColor = Colors.white.withAlpha(0.75)
            zIndex = ZIndex.InfoContainer.get()
        }
        styledDiv {
            css {
                zIndex = ZIndex.Info.get()
                +MainStyles.info
            }
            styledDiv {
                css {
                    position = Position.absolute
                    right = (-20).px
                    top = (-28).px
                    zIndex = ZIndex.InfoClose.get()
                }
                styledI {
                    css {
                        classes = mutableListOf("bi-x-circle")
                        color = Colors.darkGrey
                        fontSize = 18.px
                    }
                    attrs.onClickFunction = props.close
                }
            }
            styledDiv {
                css {
                    overflowX = Overflow.hidden
                    overflowY = Overflow.scroll
                    height = 100.pct
                    +MainStyles.padding
                    lineHeight = LineHeight("22px")
                    zIndex = ZIndex.Info.get()
                }
                styledP {
                    css {
                        fontWeight = FontWeight.bold
                    }
                    +"Over deze app"
                }
                p {
                    +"Parkeer Assistent is een persoonlijke app die niet verbonden is aan de gemeente Amsterdam."
                }
                p {
                    +"Het doel van de app is het bieden van een alternatieve, meer gebruiksvriendelijke interface voor de "
                    a("https://aanmeldenparkeren.amsterdam.nl") {
                        +"website van de gemeente"
                    }
                    +"."
                }
                p {
                    +"De app slaat geen gebruikersdata zoals meldcode en pincode of enige andere gegevens op."
                }
                p {
                    +"De maker van de app is niet aansprakelijk voor enige vorm van gebruik."
                }
                p {
                    +"De broncode van deze app is open source en beschikbaar via "
                    a("https://github.com/nilsbrenkman/parkeer-assistent") {
                        +"GitHub"
                    }
                    +"."
                }
                p {
                    +"Laat me weten wat je van de app vindt via het "
                    a("/feedback") {
                        +"feedback formulier"
                    }
                    +"."
                }
            }
        }
    }

}
