package nl.parkeerassistent.html

import kotlinx.html.*

fun HTML.application() {
    head {
        title("Parkeer Assistent")
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css"
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css"
        }
        link {
            rel = "stylesheet"
            type = "text/css"
            href = "/static/style.css"
        }
        meta {
            name = "viewport"
            content = "width=375, initial-scale=1"
        }
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/ParkeerAssistent.js") {}
    }
}

