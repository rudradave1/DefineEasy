package com.rudra.defineeasy.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rudra.defineeasy.core.CrashReporter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.reviewPromptDataStore by preferencesDataStore(name = "review_prompt_prefs")

@Singleton
class ReviewPromptPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val promptCountKey = intPreferencesKey("review_prompt_count")
    private val totalReviewsKey = intPreferencesKey("total_reviews_for_prompt")

    companion object {
        private const val MAX_PROMPT_COUNT = 2
        private const val REVIEWS_BEFORE_PROMPT = 10
    }

    fun shouldShowPrompt(): Flow<Boolean> {
        return context.reviewPromptDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    CrashReporter.logNonFatal(exception)
                    emit(emptyPreferences())
                }
            }
            .map { preferences ->
                val promptCount = preferences[promptCountKey] ?: 0
                val totalReviews = preferences[totalReviewsKey] ?: 0
                promptCount < MAX_PROMPT_COUNT && totalReviews >= REVIEWS_BEFORE_PROMPT
            }
    }

    suspend fun getShouldShowPrompt(): Boolean {
        return shouldShowPrompt().first()
    }

    fun getShouldShowPromptSync(): Boolean {
        return try {
            runBlocking {
                val prefs = context.reviewPromptDataStore.data.first()
                val promptCount = prefs[promptCountKey] ?: 0
                val totalReviews = prefs[totalReviewsKey] ?: 0
                promptCount < MAX_PROMPT_COUNT && totalReviews >= REVIEWS_BEFORE_PROMPT
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun recordPromptShown() {
        context.reviewPromptDataStore.edit { preferences ->
            val currentCount = preferences[promptCountKey] ?: 0
            preferences[promptCountKey] = currentCount + 1
        }
    }

    suspend fun incrementReviewCount() {
        context.reviewPromptDataStore.edit { preferences ->
            val currentCount = preferences[totalReviewsKey] ?: 0
            preferences[totalReviewsKey] = currentCount + 1
        }
    }

    suspend fun reset() {
        context.reviewPromptDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
