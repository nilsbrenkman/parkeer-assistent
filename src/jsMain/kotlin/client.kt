import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import nl.parkeerassistent.App
import react.child
import react.dom.render

val scope = MainScope()

fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            child(App) {}
        }
    }
}
