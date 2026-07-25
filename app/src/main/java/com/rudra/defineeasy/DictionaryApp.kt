package com.rudra.defineeasy

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.WorkerFactory
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.ReviewManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rudra.defineeasy.core.CrashReporter
import com.rudra.defineeasy.core.analytics.AnalyticsService
import com.rudra.defineeasy.notifications.DictionaryAppNotificationChannel
import com.rudra.defineeasy.notifications.ReviewReminderScheduler
import com.rudra.defineeasy.preferences.ReviewPromptPreferences
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val TAG = "DictionaryApp"

@HiltAndroidApp
class DictionaryApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var reviewReminderScheduler: ReviewReminderScheduler
    @Inject lateinit var reviewPromptPreferences: ReviewPromptPreferences
    @Inject lateinit var analyticsService: AnalyticsService

    private val startupExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Background startup task failed", throwable)
        CrashReporter.logNonFatal(throwable)
    }

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + startupExceptionHandler
    )

    override fun onCreate() {
        super.onCreate()
        initializeFirebaseCrashlytics()
        initializeAnalytics()
        createNotificationChannelsSafely()
        scheduleReviewRemindersSafely()
        checkAndShowReviewPrompt()
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

    private fun initializeAnalytics() {
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            analyticsService.initialize()
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

    private fun checkAndShowReviewPrompt() {
        runCatching {
            if (reviewPromptPreferences.getShouldShowPromptSync()) {
                val manager: ReviewManager = ReviewManagerFactory.create(this)
                manager.requestReviewFlow().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        runBlocking {
                            reviewPromptPreferences.recordPromptShown()
                        }
                    }
                }
            }
        }.onFailure { throwable ->
            Log.e(TAG, "Review prompt check failed", throwable)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(
                runCatching { workerFactory }
                    .getOrElse {
                        Log.e(TAG, "Hilt workerFactory unavailable; using default factory", it)
                        object : WorkerFactory() {
                            override fun createWorker(
                                appContext: Context,
                                workerClassName: String,
                                workerParameters: WorkerParameters
                            ): ListenableWorker? = null
                        }
                    }
            )
            .build()
}
