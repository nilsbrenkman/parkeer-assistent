package nl.parkeerassistent.android.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import nl.parkeerassistent.android.NotificationReceiver
import nl.parkeerassistent.android.R
import nl.parkeerassistent.android.data.Parking
import nl.parkeerassistent.android.data.ParkingResponse
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KProperty1

class NotificationUtil @Inject constructor() {

    var previous: ParkingResponse? = null

    fun scheduleNotifications(context: Context?, parking: ParkingResponse) {
        context ?: return
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        cancelPrevious(context, manager)
        previous = parking

        val scheduleNotification: (Parking, NotificationTime) -> Unit = { p, n ->
            val title = context.getString(n.title)
            val content = "${p.name ?: "?"} | [ ${LicenseUtil.format(p.license)} ]"

            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra(NotificationReceiver.NOTIFICATION_TITTLE_KEY, title)
            intent.putExtra(NotificationReceiver.NOTIFICATION_CONTENT_KEY, content)

            val notification = PendingIntent.getBroadcast(context, p.id * n.idMultiplier, intent, PendingIntent.FLAG_IMMUTABLE)
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, n.dateProperty(p).time, notification)
        }

        parking.active.forEach { p ->
            scheduleNotification(p, NotificationTime.END)
        }
        parking.scheduled.forEach { p ->
            scheduleNotification(p, NotificationTime.START)
            scheduleNotification(p, NotificationTime.END)
        }
    }

    private fun cancelPrevious(context: Context, manager: AlarmManager) {
        val parking = previous ?: return

        val cancelNotification: (Parking, NotificationTime) -> Unit = { p, n ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pending = PendingIntent.getBroadcast(context, p.id * n.idMultiplier, intent, PendingIntent.FLAG_IMMUTABLE)
            manager.cancel(pending)
        }
        parking.active.forEach { p ->
            cancelNotification(p, NotificationTime.END)
        }
        parking.scheduled.forEach { p ->
            cancelNotification(p, NotificationTime.START)
            cancelNotification(p, NotificationTime.END)
        }
    }

    enum class NotificationTime(val dateProperty: KProperty1<Parking, Date>, val title: Int, val idMultiplier: Int) {
        START(Parking::startDate, R.string.notification_title_start, -1),
        END  (Parking::endDate,   R.string.notification_title_end,    1),
        ;
    }

}

