package nl.parkeerassistent.style

import kotlinx.css.BorderStyle
import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FontWeight
import kotlinx.css.LinearDimension
import kotlinx.css.Outline
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.body
import kotlinx.css.borderColor
import kotlinx.css.borderLeftWidth
import kotlinx.css.borderRadius
import kotlinx.css.borderRightWidth
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.fontFamily
import kotlinx.css.fontSize
import kotlinx.css.fontWeight
import kotlinx.css.h3
import kotlinx.css.h4
import kotlinx.css.height
import kotlinx.css.input
import kotlinx.css.label
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.marginTop
import kotlinx.css.maxWidth
import kotlinx.css.outline
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.properties.border
import kotlinx.css.properties.boxShadow
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.top
import kotlinx.css.vh
import kotlinx.css.width
import kotlinx.css.zIndex

var Style = CSSBuilder().apply {

    body {
        fontFamily = "\"Gotham SSm A\", \"Gotham SSm B\", \"Open Sans\", Helvetica, Arial, sans-serif"
        backgroundColor = Color("#dddddd")
        padding(0.rem)
        margin(0.rem)
    }

    h3 {
        color = Color.white
        backgroundColor = Color("#004085")
        textAlign = TextAlign.center
        fontSize = 18.px
        fontWeight = FontWeight.bold
        padding(12.px, 16.px)
    }

    h4 {
        color = Color("#004085")
        backgroundColor = Color("#cfe2ff")
        textAlign = TextAlign.center
        fontSize = 16.px
        padding(8.px, 16.px)
    }

    label {
        display = Display.block
    }

    input {
        width = 100.pct
        border(1.px, BorderStyle.solid, Color("#dddddd"), 2.px)
        padding(1.px, 6.px)

        focus {
            borderColor = Color("#555555")
            outline = Outline.none
        }
    }

    rule("#root") {
        position = Position.relative
        marginLeft = LinearDimension.auto
        marginRight = LinearDimension.auto
        height = 100.vh
        maxWidth = 400.px
        backgroundColor = Color("#ffffff")
        boxShadow(Color.black.withAlpha(0.15), 0.px, 0.px, 15.px, 5.px)
    }

    rule(".btn-block") {
        borderRadius = 0.px
        borderLeftWidth = 0.px
        borderRightWidth = 0.px
    }

    rule(".spinner-border") {
        position = Position.fixed
        top = 50.pct
        left = 50.pct
        marginTop = (-12).px
        marginLeft = (-12).px
        zIndex = ZIndex.Spinner.get()
    }

}