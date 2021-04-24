package nl.parkeerassistent

import Button
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import nl.parkeerassistent.message.Message
import nl.parkeerassistent.message.messageBroker
import nl.parkeerassistent.model.AddVisitorRequest
import nl.parkeerassistent.model.Response
import nl.parkeerassistent.model.Visitor
import nl.parkeerassistent.model.VisitorResponse
import nl.parkeerassistent.style.Elements
import nl.parkeerassistent.style.MainStyles
import org.w3c.dom.HTMLInputElement
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
import styled.styledInput

external interface AddVisitorProps : RProps {
    var onSuccess: (visitors: List<Visitor>, selected: Visitor) -> Unit
    var close: () -> Unit
}

val AddVisitor = functionalComponent<AddVisitorProps> { props ->

    val (license, setLicense) = useState("")
    val (name, setName) = useState("")

    fun getAddedVisitor() {
        scope.launch {
            val result = client.get<VisitorResponse>(api("visitor"))
            for (visitor in result.visitors) {
                if (visitor.name == name) {
                    props.onSuccess(result.visitors, visitor)
                }
            }
        }
    }

    fun addVisitor() {
        spinner.start()
        scope.launch {
            val result = client.post<Response> {
                url(api("visitor"))
                contentType(ContentType.Application.Json)
                body = AddVisitorRequest(License.format(license), name)
            }
            if (result.success) {
                messageBroker.show(Message("Bezoeker toegevoegd", Message.MessageType.SUCCESS))
                getAddedVisitor()
            } else {
                if (result.message != null) {
                    messageBroker.show(Message(result.message, Message.MessageType.ERROR))
                } else {
                    messageBroker.show(Message("Onbekende fout", Message.MessageType.ERROR))
                }
            }
        }
    }

    form {
        styledDiv {
            css {
                +MainStyles.padding
            }
            label {
                +"Kenteken:"
            }
            styledInput(InputType.text, name = "license") {
                css {
                    +MainStyles.license
                }
                attrs.value = License.format(license)
                attrs.onChangeFunction = { event -> setLicense(License.normalise((event.target as HTMLInputElement).value)) }
            }
        }
        styledDiv {
            css {
                +MainStyles.padding
            }
            label {
                +"Naam:"
            }
            input(InputType.text, name = "visitor") {
                attrs.value = name
                attrs.onChangeFunction = { event -> setName((event.target as HTMLInputElement).value) }
            }
        }

        child(Elements.Spacer) {}

        Button {
            attrs.variant = "success"
            attrs.block = true
            attrs.onClick = {
                addVisitor()
            }
            +"Toevoegen"
        }

        Button {
            attrs.variant = "outline-danger"
            attrs.block = true
            attrs.onClick = {
                props.close()
            }
            +"Annuleren"
        }
    }

}