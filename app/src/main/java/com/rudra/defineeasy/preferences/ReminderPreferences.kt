package com.rudra.defineeasy.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.reminderDataStore by preferencesDataStore(name = "reminder_prefs")

data class ReminderSettings(
    val enabled: Boolean = true,
    val hour: Int = 20,
    val minute: Int = 0
)

@Singleton
class ReminderPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val enabledKey = booleanPreferencesKey("reminder_enabled")
    private val hourKey = intPreferencesKey("reminder_hour")
    private val minuteKey = intPreferencesKey("reminder_minute")

    fun reminderSettings(): Flow<ReminderSettings> {
        return context.reminderDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                ReminderSettings(
                    enabled = preferences[enabledKey] ?: true,
                    hour = preferences[hourKey] ?: 20,
                    minute = preferences[minuteKey] ?: 0
                )
            }
    }

    suspend fun getReminderSettings(): ReminderSettings {
        return reminderSettings().first()
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.reminderDataStore.edit { preferences ->
            preferences[enabledKey] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.reminderDataStore.edit { preferences ->
            preferences[hourKey] = hour
            preferences[minuteKey] = minute
        }
    }
}
