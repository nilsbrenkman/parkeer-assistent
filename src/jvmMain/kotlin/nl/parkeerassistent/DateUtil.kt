package nl.parkeerassistent

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    val amsterdam = TimeZone.getTimeZone("Europe/Amsterdam")

    val dateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
    val date = SimpleDateFormat("yyyy-MM-dd")

}