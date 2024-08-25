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
import com.erendogan6.havatahminim.ui.view.MainActivity

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        sendNotification(context)
        NotificationUtils.scheduleDailyNotification(context)
    }

    private fun sendNotification(context: Context) {
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
                .setContentTitle("Hava Durumu Hatırlatma")
                .setContentText("Bugünün Hava Durumunu Kontrol Ettin Mi?")
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
