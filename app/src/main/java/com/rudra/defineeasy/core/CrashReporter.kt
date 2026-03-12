package com.rudra.defineeasy.core

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rudra.defineeasy.BuildConfig

object CrashReporter {
    fun logNonFatal(throwable: Throwable) {
        if (BuildConfig.CRASHLYTICS_ENABLED) {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }
}
