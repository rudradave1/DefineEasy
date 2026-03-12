package com.rudra.defineeasy.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rudra.defineeasy.core.CrashReporter
import com.rudra.defineeasy.preferences.ReminderPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val reminderPreferences: ReminderPreferences
) {
    suspend fun syncSchedule() {
        try {
            val settings = reminderPreferences.getReminderSettings()
            if (settings.enabled) {
                schedule(settings.hour, settings.minute)
            } else {
                cancel()
            }
        } catch (throwable: Throwable) {
            CrashReporter.logNonFatal(throwable)
        }
    }

    fun schedule(hour: Int, minute: Int) {
        val workRequest = PeriodicWorkRequestBuilder<ReviewReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(hour, minute), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReviewReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun reschedule(hour: Int, minute: Int) {
        try {
            cancel()
            schedule(hour, minute)
        } catch (throwable: Throwable) {
            CrashReporter.logNonFatal(throwable)
        }
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(ReviewReminderWorker.WORK_NAME)
    }

    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var nextRun = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(now, nextRun).toMillis()
    }
}
