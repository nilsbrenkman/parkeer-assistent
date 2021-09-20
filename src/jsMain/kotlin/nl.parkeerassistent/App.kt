package nl.parkeerassistent

import Button
import Spinner
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinext.js.jsObject
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import nl.parkeerassistent.message.Message
import nl.parkeerassistent.message.MessageList
import nl.parkeerassistent.message.messageBroker
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.style.Colors
import nl.parkeerassistent.style.Elements
import nl.parkeerassistent.style.MainStyles
import nl.parkeerassistent.style.ZIndex
import react.*
import scope
import styled.css
import styled.styledDiv
import styled.styledImg

val client = HttpClient {
    install(JsonFeature) { serializer = KotlinxSerializer() }
}

fun api(path: String): String {
    return "${window.location.origin}/$path"
}

object spinner {

    var setSpinner: ((Boolean) -> Unit)? = null

    fun register(setSpinner: (Boolean) -> Unit) {
        this.setSpinner = setSpinner
    }

    fun start() {
        this.setSpinner?.invoke(true)
    }

    fun stop() {
        this.setSpinner?.invoke(false)
    }

}

val App = functionalComponent<RProps> {

    val (loading, setLoading) = useState(true)
    val (loggedIn, setLoggedIn) = useState(false)
    val (spinning, setSpinning) = useState(false)
    val (info, setInfo) = useState(false)

    fun logout() {
        scope.launch {
            val result = client.get<Response>(api("logout"))
            if (result.success) {
                setLoggedIn(false)
            } else {
                if (result.message != null) {
                    messageBroker.show(Message(result.message, Message.MessageType.ERROR))
                } else {
                    messageBroker.show(Message("Onbekende fout", Message.MessageType.ERROR))
                }
            }
        }
    }

    suspend fun loggedIn() {
        val result = client.get<Response>(api("login"))
        if (result.success) {
            setLoggedIn(true)
        }
        setLoading(false)
    }

    useEffect(dependencies = listOf()) {
        spinner.register(setSpinning)
        scope.launch {
            loggedIn()
        }
    }

    styledDiv {
        css {
            position = Position.fixed
            backgroundColor = Colors.header
            width = 100.pct
            maxWidth = 400.px
            zIndex = ZIndex.Header.get()
            +MainStyles.padding
        }
        styledImg("logo", src = "/static/logo-transparent.svg") {
            css {
                height = 40.px
            }
            attrs.onClickFunction = { setInfo(!info) }
        }
        if (loggedIn) {
            styledDiv {
                css {
                    position = Position.absolute
                    top = 50.pct
                    marginTop = (-18).px
                    right = MainStyles.marginHorizontal
                }
                Button {
                    attrs.variant = "outline-light"
                    attrs.onClick = {
                        logout()
                    }
                    +"Log uit"
                }
            }
        }
    }
    styledDiv {
        css {
            display = if (spinning) Display.block else Display.none
        }
        styledDiv {
            css {
                position = Position.fixed
                width = 100.pct
                height = 100.pct
                backgroundColor = Colors.darkGrey.withAlpha(0.25)
                zIndex = ZIndex.SpinnerContainer.get()
            }
        }
        Spinner {
            attrs.animation = "border"
        }
    }
    styledDiv {
        css {
            position = Position.relative
            top = 64.px
            width = 100.pct
        }

        child(MessageList) {}

        child(Elements.Spacer) {}

        if (loading) {
            Spinner {
                attrs.animation = "border"
            }
        } else {
            if (loggedIn) {
                child(User) {}
            } else {
                child(Login) {}
            }
        }

        child(Elements.Spacer) {}

    }

    child(Info, props = jsObject<InfoProps> {
        visible = info
        close = { setInfo(false) }
    }) {}

}

