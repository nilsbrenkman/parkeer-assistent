package nl.parkeerassistent.html

import kotlinx.css.*
import kotlinx.css.properties.boxShadow
import kotlinx.html.*

private var Style = CSSBuilder().apply {
    body {
        backgroundColor = Color("#dddddd")
        padding(0.rem)
        margin(0.rem)
    }
    "#container" {
        marginLeft = LinearDimension.auto
        marginRight = LinearDimension.auto
        height = 100.vh
        maxWidth = 400.px
        backgroundColor = Color("#ffffff")
        boxShadow(Color.black.withAlpha(0.15), 0.px, 0.px, 15.px, 5.px)
    }
    "#header" {
        width = 100.pct
        backgroundColor = Color("#007cbc")

    }
    "#logo" {
        margin(12.px, 18.px)
        height = 40.px
    }
    "#form" {
        width = 100.pct
        height = LinearDimension("calc(100vh - 72px)")
        borderWidth = 0.px
    }
}

fun HTML.feedback() {
    head {
        title("Feedback | Parkeer Assistent")
        meta {
            name = "viewport"
            content = "initial-scale=1"
        }
    }
    body {
        div {
            id = "container"
            div {
                id = "header"
                img {
                    id = "logo"
                    src = "/static/logo-transparent.svg"
                }
            }
            div {
                id = "main"
                iframe {
                    id = "form"
                    src = "https://docs.google.com/forms/d/e/1FAIpQLSfhOv4kdt-8CORXakwPOm92zJQaFWOiPNV0LbzsDqD0EwN4Mg/viewform?embedded=true"
                    +"Laden…"
                }
            }
        }
        style {
            +Style.toString()
        }
    }
}