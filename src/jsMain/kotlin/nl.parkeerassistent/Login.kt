package nl.parkeerassistent

import Button
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.html.FormEncType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import nl.parkeerassistent.message.Message
import nl.parkeerassistent.message.messageBroker
import nl.parkeerassistent.model.LoginRequest
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.style.Elements
import nl.parkeerassistent.style.MainStyles
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.RProps
import react.child
import react.dom.form
import react.dom.input
import react.dom.label
import react.functionalComponent
import react.useState
import scope
import styled.css
import styled.styledDiv

val Login = functionalComponent<RProps> {

    val (username, setUsername) = useState("")
    val (password, setPassword) = useState("")

    val login = fun(event: Event) {
        val form = (event.target as HTMLButtonElement).form
        scope.launch {
            val result = client.post<Response> {
                url(api("login"))
                contentType(ContentType.Application.Json)
                body = LoginRequest(username, password)
            }
            if (result.success) {
                form?.submit()
            } else {
                if (result.message != null) {
                    messageBroker.show(Message(result.message, Message.MessageType.ERROR))
                } else {
                    messageBroker.show(Message("Onbekende fout", Message.MessageType.ERROR))
                }
            }
        }
    }

    form("/", FormEncType.applicationXWwwFormUrlEncoded, FormMethod.post) {
        attrs.id = "loginForm"
        styledDiv {
            css {
                +MainStyles.padding
            }
            label {
                +"Meldcode:"
            }
            input(InputType.text, name = "username") {
                attrs.value = username
                attrs.onChangeFunction = { event -> setUsername((event.target as HTMLInputElement).value) }
            }
        }
        styledDiv {
            css {
                +MainStyles.padding
            }
            label {
                +"Pincode:"
            }
            input(InputType.password, name = "password") {
                attrs.value = password
                attrs.onChangeFunction = { event -> setPassword((event.target as HTMLInputElement).value) }
            }
        }

        child(Elements.Spacer) {}

        Button {
            attrs.variant = "success"
            attrs.onClick = login
            attrs.block = true
            +"Inloggen"
        }
    }

}
