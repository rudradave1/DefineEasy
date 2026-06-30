package com.rudra.defineeasy.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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

private val Context.streakDataStore by preferencesDataStore(name = "streak_prefs")

data class StreakState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastReviewEpochDay: Long = -1,
    val freezeCount: Int = 1,
    val totalReviews: Int = 0
)

@Singleton
class StreakPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val currentStreakKey = intPreferencesKey("current_streak")
    private val longestStreakKey = intPreferencesKey("longest_streak")
    private val lastReviewEpochDayKey = longPreferencesKey("last_review_epoch_day")
    private val freezeCountKey = intPreferencesKey("freeze_count")
    private val totalReviewsKey = intPreferencesKey("total_reviews")

    fun streakState(): Flow<StreakState> {
        return context.streakDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    CrashReporter.logNonFatal(exception)
                    emit(emptyPreferences())
                }
            }
            .map { preferences ->
                StreakState(
                    currentStreak = preferences[currentStreakKey] ?: 0,
                    longestStreak = preferences[longestStreakKey] ?: 0,
                    lastReviewEpochDay = preferences[lastReviewEpochDayKey] ?: -1,
                    freezeCount = preferences[freezeCountKey] ?: 1,
                    totalReviews = preferences[totalReviewsKey] ?: 0
                )
            }
    }

    suspend fun getStreakState(): StreakState {
        return streakState().first()
    }

    suspend fun recordReview(todayEpochDay: Long) {
        context.streakDataStore.edit { preferences ->
            val lastReviewDay = preferences[lastReviewEpochDayKey] ?: -1
            val currentStreak = preferences[currentStreakKey] ?: 0
            val longestStreak = preferences[longestStreakKey] ?: 0
            val totalReviews = (preferences[totalReviewsKey] ?: 0) + 1

            val newStreak = when {
                lastReviewDay == todayEpochDay -> currentStreak
                lastReviewDay == todayEpochDay - 1 -> currentStreak + 1
                else -> 1
            }

            preferences[currentStreakKey] = newStreak
            preferences[longestStreakKey] = maxOf(longestStreak, newStreak)
            preferences[lastReviewEpochDayKey] = todayEpochDay
            preferences[totalReviewsKey] = totalReviews
        }
    }

    suspend fun useStreakFreeze(): Boolean {
        val state = getStreakState()
        if (state.freezeCount <= 0) return false

        context.streakDataStore.edit { preferences ->
            preferences[freezeCountKey] = state.freezeCount - 1
        }
        return true
    }

    suspend fun addStreakFreeze() {
        context.streakDataStore.edit { preferences ->
            val current = preferences[freezeCountKey] ?: 1
            preferences[freezeCountKey] = current + 1
        }
    }

    suspend fun resetStreak() {
        context.streakDataStore.edit { preferences ->
            preferences[currentStreakKey] = 0
            preferences[lastReviewEpochDayKey] = -1
        }
    }
}
