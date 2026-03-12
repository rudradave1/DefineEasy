package com.rudra.defineeasy.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.rudra.defineeasy.R

object DictionaryAppNotificationChannel {
    const val REVIEW_REMINDER_CHANNEL_ID = "review_reminder"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            REVIEW_REMINDER_CHANNEL_ID,
            context.getString(R.string.review_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }
}
