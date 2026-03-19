package com.rudra.defineeasy

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.rudra.defineeasy.notifications.DictionaryAppNotificationChannel
import com.rudra.defineeasy.notifications.ReviewReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "DictionaryApp"

@HiltAndroidApp
class DictionaryApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var reviewReminderScheduler: ReviewReminderScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // FIX 3 — Detect accidental main-thread I/O in debug builds only.
        // penaltyLog() prints to Logcat; never used in release.
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

        // FIX 5 — Top-level guard: if any initialisation step throws we log
        // the error and continue rather than crashing the process.
        try {

            // FIX 1 — Firebase / Crashlytics: guard individually so a missing
            // or invalid google-services.json never kills the app.
            try {
                FirebaseApp.initializeApp(this)
                FirebaseCrashlytics.getInstance()
                    .setCrashlyticsCollectionEnabled(BuildConfig.CRASHLYTICS_ENABLED)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase/Crashlytics init failed — continuing without it", e)
            }

            DictionaryAppNotificationChannel.createChannels(this)

            applicationScope.launch {
                reviewReminderScheduler.syncSchedule()
            }

        } catch (e: Exception) {
            // Last-resort catch: log and survive rather than crashing.
            Log.e(TAG, "Critical error during app initialisation", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
