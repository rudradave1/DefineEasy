package com.rudra.defineeasy.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rudra.defineeasy.MainActivity
import com.rudra.defineeasy.R
import com.rudra.defineeasy.feature_dictionary.data.local.WordInfoDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class ReviewReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: WordInfoDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dueCount = database.dao.getDueReviewCountValue(LocalDate.now().toEpochDay())
        if (dueCount <= 0) {
            return Result.success()
        }

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_TAB, MainActivity.TAB_REVIEW)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, DictionaryAppNotificationChannel.REVIEW_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(applicationContext.getString(R.string.review_notification_title))
            .setContentText(
                applicationContext.getString(R.string.review_notification_body, dueCount)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(REVIEW_NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_review_reminder"
        private const val REVIEW_NOTIFICATION_ID = 1002
    }
}
