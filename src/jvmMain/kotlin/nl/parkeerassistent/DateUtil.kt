package nl.parkeerassistent

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.TimeZone

object DateUtil {

    val amsterdam = TimeZone.getTimeZone("Europe/Amsterdam")

    val dateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
    val date = SimpleDateFormat("yyyy-MM-dd")

    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX").withZone(amsterdam.toZoneId())

}