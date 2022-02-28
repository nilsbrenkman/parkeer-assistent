package nl.parkeerassistent.android

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import nl.parkeerassistent.android.ui.AppActivity
import kotlin.random.Random

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_TITTLE_KEY = "title"
        const val NOTIFICATION_CONTENT_KEY = "content"

        val NOTIFICATION_SOUND = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/raw/car_horn")
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        val (title, content) = intent?.run {
            getStringExtra(NOTIFICATION_TITTLE_KEY) to getStringExtra(NOTIFICATION_CONTENT_KEY)
        } ?: (null to null)

        with(context) {
            Log.i("NotificationReceiver", "Showing notification: $content")

            val builder = NotificationCompat.Builder(this, BuildConfig.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(IconCompat.createWithResource(context, R.drawable.logo_notification))
                .setLargeIcon(getLargeIcon(context))
                .setSound(NOTIFICATION_SOUND)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(getContentIntent(context))

            with(NotificationManagerCompat.from(this)) {
                notify(Random.Default.nextInt(), builder.build())
            }
        }
    }

    private fun getLargeIcon(context: Context): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, R.drawable.logo_notification) ?: return null
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0,0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getContentIntent(context: Context): PendingIntent {
        val notificationIntent = Intent(context, AppActivity::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return contentIntent
    }

}