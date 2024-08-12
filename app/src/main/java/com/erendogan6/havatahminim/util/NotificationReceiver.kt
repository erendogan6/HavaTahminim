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
        val notificationIntent = Intent(context, MainActivity::class.java)

        val pendingIntent: PendingIntent =
            TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(notificationIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

        val builder =
            NotificationCompat
                .Builder(context, "daily_notification_channel")
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle("Hava Durumu Hatırlatma")
                .setContentText("Bugünün Hava Durumunu Kontrol Ettin Mi?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        val channelName = "Daily Notification Channel"
        val channelDescription = "Channel for daily weather check notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel("daily_notification_channel", channelName, importance).apply {
                description = channelDescription
            }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        sendNotification(context, builder)
    }

    private fun sendNotification(
        context: Context,
        builder: NotificationCompat.Builder,
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(0, builder.build())
            }
        } else {
            notificationManager.notify(0, builder.build())
        }
    }
}
