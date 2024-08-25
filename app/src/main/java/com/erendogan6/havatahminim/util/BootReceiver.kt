package com.erendogan6.havatahminim.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.erendogan6.havatahminim.util.NotificationUtils.scheduleDailyNotification

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleDailyNotification(context)
        }
    }
}
