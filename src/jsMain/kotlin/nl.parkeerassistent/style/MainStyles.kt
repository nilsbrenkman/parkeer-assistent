package nl.parkeerassistent.style

import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.borderBottom
import kotlinx.css.properties.border
import styled.StyleSheet

object MainStyles : StyleSheet("MainStyles", isStatic = true) {

    val marginHorizontal = 18.px
    val marginVertical = 12.px
    val spacer = 18.px

    val containerMaxWidth = 400.px
    val licenseWidth = 140

    val license by css {
        display = Display.inlineBlock
        width = licenseWidth.px
        backgroundColor = Colors.license
        border(1.px, BorderStyle.solid, Colors.black, 4.px)
        letterSpacing = 2.px
        fontSize = 18.px
        fontWeight = FontWeight.bold
        lineHeight = LineHeight("36px")
        textAlign = TextAlign.center
    }

    val balance by css {
        position = Position.fixed
        textAlign = TextAlign.right
        marginTop = spacer.times(-1)
        width = 100.pct
        maxWidth = containerMaxWidth
        color = Colors.header
        backgroundColor = Colors.white
        padding(2.px, marginHorizontal)
        borderBottom(1.px, BorderStyle.solid, Colors.header)
    }

    val icon by css {
        position = Position.absolute
        top = 12.px
        left = 16.px
        fontSize = 1.8.em
        textAlign = TextAlign.center
    }

    val visitorName by css {
        fontSize = 18.px
        fontWeight = FontWeight.bold
    }

    val padding by css {
        padding(marginVertical, marginHorizontal)
    }

    val third by css {
        display = Display.inlineBlock
        width = 30.pct
        div {
            width = 100.pct
            padding(12.px)
            backgroundColor = Colors.lightestGrey
            textAlign = TextAlign.center
            border(1.px, BorderStyle.solid, Colors.darkGrey)
            borderRadius = 4.px
            fontSize = 18.px
            fontWeight = FontWeight.bold
        }
        adjacentSibling(".MainStyles-third") {
            marginLeft = 5.pct
        }
    }

    val info by css {
        position = Position.absolute
        top = 20.pct
        left = 10.pct
        width = 80.pct
        height = 60.pct
        border(1.px, BorderStyle.solid, Colors.darkGrey, 6.px)
        color = Colors.darkGrey
        backgroundColor = Colors.white
        fontSize = 12.px
    }

}