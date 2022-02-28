package nl.parkeerassistent.android

import android.app.Application
import android.media.AudioAttributes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class ParkeerAssistent : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val id = BuildConfig.NOTIFICATION_CHANNEL_ID
        val name = getString(R.string.notification_channel_name)
        val description = getString(R.string.notification_channel_description)
        val importance = NotificationManagerCompat.IMPORTANCE_MAX

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channel = NotificationChannelCompat.Builder(id, importance)
            .setName(name)
            .setImportance(importance)
            .setDescription(description)
            .setSound(NotificationReceiver.NOTIFICATION_SOUND, audioAttributes)
            .build()

        // Register the channel with the system
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.createNotificationChannel(channel)
    }

}
