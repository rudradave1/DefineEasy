package com.rudra.defineeasy

import android.app.Application
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

@HiltAndroidApp
class DictionaryApp: Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var reviewReminderScheduler: ReviewReminderScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(BuildConfig.CRASHLYTICS_ENABLED)
        DictionaryAppNotificationChannel.createChannels(this)
        applicationScope.launch {
            reviewReminderScheduler.syncSchedule()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
