package com.rudra.defineeasy

import android.app.Application
import android.os.StrictMode
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rudra.defineeasy.core.CrashReporter
import com.rudra.defineeasy.notifications.DictionaryAppNotificationChannel
import com.rudra.defineeasy.notifications.ReviewReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "DictionaryApp"

@HiltAndroidApp
class DictionaryApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var reviewReminderScheduler: ReviewReminderScheduler

    private val startupExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Background startup task failed", throwable)
        CrashReporter.logNonFatal(throwable)
    }

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + startupExceptionHandler
    )

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
        }

        initializeFirebaseCrashlytics()
        createNotificationChannelsSafely()
        scheduleReviewRemindersSafely()
    }

    private fun initializeFirebaseCrashlytics() {
        runCatching {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        }.onFailure { throwable ->
            Log.e(TAG, "Firebase initialization failed; continuing without it", throwable)
            CrashReporter.logNonFatal(throwable)
        }

        runCatching {
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                FirebaseCrashlytics.getInstance()
                    .setCrashlyticsCollectionEnabled(BuildConfig.CRASHLYTICS_ENABLED)
            }
        }.onFailure { throwable ->
            Log.e(TAG, "Crashlytics configuration failed; continuing without it", throwable)
        }
    }

    private fun createNotificationChannelsSafely() {
        runCatching {
            DictionaryAppNotificationChannel.createChannels(this)
        }.onFailure { throwable ->
            Log.e(TAG, "Notification channel setup failed", throwable)
            CrashReporter.logNonFatal(throwable)
        }
    }

    private fun scheduleReviewRemindersSafely() {
        applicationScope.launch {
            runCatching {
                reviewReminderScheduler.syncSchedule()
            }.onFailure { throwable ->
                Log.e(TAG, "Review reminder scheduling failed", throwable)
                CrashReporter.logNonFatal(throwable)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
