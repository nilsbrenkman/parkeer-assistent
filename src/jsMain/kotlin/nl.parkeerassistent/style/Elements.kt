package nl.parkeerassistent.style

import kotlinx.css.*
import react.RProps
import react.functionalComponent
import styled.css
import styled.styledDiv
import styled.styledHr

object Elements {

    val Spacer = functionalComponent<RProps> {
        styledDiv {
            css {
                height = MainStyles.spacer
            }
        }
    }

    val Separator = functionalComponent<RProps> {
        styledHr {
            css {
                margin(0.px)
                borderColor = Colors.lightGrey
            }
        }
    }

}