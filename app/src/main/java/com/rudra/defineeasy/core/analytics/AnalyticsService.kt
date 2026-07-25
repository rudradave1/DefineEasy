package com.rudra.defineeasy.core.analytics

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.rudra.defineeasy.BuildConfig
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around Firebase Analytics that guards against uninitialized Firebase
 * and respects the debug-only flag for Crashlytics parity.
 */
@Singleton
class AnalyticsService @Inject constructor() {

    private var analytics: FirebaseAnalytics? = null

    /** Call once after FirebaseApp.initializeApp succeeds. */
    fun initialize() {
        analytics = try {
            Firebase.analytics
        } catch (_: IllegalStateException) {
            null // Firebase not initialized
        }
    }

    // ── Screen tracking ────────────────────────────────────────────────

    fun trackScreen(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // ── User actions ───────────────────────────────────────────────────

    fun onSearch(query: String) {
        logEvent("search", "query" to truncate(query, 100))
    }

    fun onWordViewed(word: String) {
        logEvent("word_viewed", "word" to truncate(word, 50))
    }

    fun onFavoriteToggled(word: String, isFavorited: Boolean) {
        logEvent(
            if (isFavorited) "favorite_added" else "favorite_removed",
            "word" to truncate(word, 50)
        )
    }

    fun onWordShared(word: String) {
        logEvent("word_shared", "word" to truncate(word, 50))
    }

    fun onReviewCompleted(count: Int) {
        logEvent("review_completed", "count" to count)
    }

    fun onWordRated(word: String, rating: Int) {
        logEvent("word_rated", "word" to truncate(word, 50), "rating" to rating)
    }

    fun onQuizCompleted(score: Int, total: Int) {
        logEvent("quiz_completed", "score" to score, "total" to total)
    }

    fun onQuizStarted() {
        logEvent("quiz_started")
    }

    fun onCollectionOpened(collectionId: String) {
        logEvent("collection_opened", "collection_id" to truncate(collectionId, 50))
    }

    fun onCollectionCreated() {
        logEvent("collection_created")
    }

    fun onWordOfDayViewed(word: String) {
        logEvent("word_of_day_viewed", "word" to truncate(word, 50))
    }

    fun onOnboardingCompleted() {
        logEvent("onboarding_completed")
    }

    fun onReminderEnabled(enabled: Boolean) {
        logEvent("reminder_toggled", "enabled" to enabled)
    }

    fun onSettingsOpened() {
        logEvent("settings_opened")
    }

    fun onDeepLinkOpened(word: String) {
        logEvent("deep_link_opened", "word" to truncate(word, 50))
    }

    // ── Internal ───────────────────────────────────────────────────────

    private fun logEvent(name: String, vararg params: Pair<String, Any>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                }
            }
        }
        analytics?.logEvent(name, bundle)
    }

    private fun truncate(value: String, max: Int): String =
        if (value.length <= max) value else value.take(max)
}
