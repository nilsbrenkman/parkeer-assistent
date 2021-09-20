package nl.parkeerassistent.style

import kotlinx.css.*
import kotlinx.css.properties.border
import kotlinx.css.properties.boxShadow

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