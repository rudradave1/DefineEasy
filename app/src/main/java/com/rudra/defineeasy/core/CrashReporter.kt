package com.rudra.defineeasy.core

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rudra.defineeasy.BuildConfig

object CrashReporter {
    private const val TAG = "CrashReporter"

    fun logNonFatal(throwable: Throwable) {
        if (BuildConfig.CRASHLYTICS_ENABLED) {
            runCatching {
                FirebaseCrashlytics.getInstance().recordException(throwable)
            }.onFailure {
                Log.e(TAG, "Failed to record non-fatal exception", it)
            }
        }
    }
}
