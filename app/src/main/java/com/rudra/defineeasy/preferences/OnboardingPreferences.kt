package com.rudra.defineeasy.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

@Singleton
class OnboardingPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    fun isOnboardingCompleted(): Flow<Boolean> {
        return context.onboardingDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[onboardingCompletedKey] ?: false
            }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = completed
        }
    }
}
