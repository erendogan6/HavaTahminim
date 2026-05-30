package com.erendogan6.havatahminim.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.erendogan6.havatahminim.R
import com.erendogan6.havatahminim.repository.WeatherRepository
import com.erendogan6.havatahminim.ui.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: WeatherRepository

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        // The notification text depends on a network fetch, so keep the receiver alive with
        // goAsync() while we resolve the pollen situation off the main thread.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (title, text) = buildNotificationContent(context)
                sendNotification(context, title, text)
            } finally {
                NotificationUtils.scheduleDailyNotification(context)
                pendingResult.finish()
            }
        }
    }

    /**
     * Returns a pollen-alert title/text when one of the user's relevant allergens is HIGH or
     * VERY_HIGH today, otherwise the generic weather reminder. Falls back to the generic message on
     * any failure (no saved location, pollen unavailable in region, network error).
     */
    private suspend fun buildNotificationContent(context: Context): Pair<String, String> {
        val generic =
            context.getString(R.string.notification_weather_title) to
                context.getString(R.string.notification_weather_text)

        val location = runCatching { repository.getSavedLocation() }.getOrNull() ?: return generic
        val airQuality =
            runCatching { repository.getAirQuality(location.latitude, location.longitude) }
                .getOrNull() ?: return generic
        if (!airQuality.pollenAvailable) return generic

        val sensitive = runCatching { repository.sensitiveAllergens() }.getOrNull().orEmpty()
        val alarming =
            airQuality.pollen
                .filter { sensitive.isEmpty() || it.type in sensitive }
                .filter { PollenLevel.isAlarming(it.risk) }

        if (alarming.isEmpty()) return generic

        val allergenList =
            alarming.joinToString(", ") { context.getString(PollenLevel.typeNameRes(it.type)) }
        return context.getString(R.string.pollen_alert_title) to
            context.getString(R.string.pollen_alert_text, allergenList)
    }

    private fun sendNotification(
        context: Context,
        title: String,
        text: String,
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_notification_channel"

        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channelName = "Daily Notification Channel"
            val channelDescription = "Channel for daily weather check notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(channelId, channelName, importance).apply {
                    description = channelDescription
                }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(notificationIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

        val builder =
            NotificationCompat
                .Builder(context, channelId)
                .setSmallIcon(R.drawable.cloudy)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } else {
            NotificationManagerCompat.from(context).notify(0, builder.build())
        }
    }
}
