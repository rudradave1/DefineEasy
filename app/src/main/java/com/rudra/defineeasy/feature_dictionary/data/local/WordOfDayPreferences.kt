package com.rudra.defineeasy.feature_dictionary.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.wordOfDayDataStore by preferencesDataStore(name = "word_of_day_prefs")

@Singleton
class WordOfDayPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val shownDateKey: Preferences.Key<String> = stringPreferencesKey("word_of_day_shown_date")

    suspend fun getShownDate(): String? {
        return context.wordOfDayDataStore.data.first()[shownDateKey]
    }

    suspend fun setShownDate(date: String) {
        context.wordOfDayDataStore.edit { preferences ->
            preferences[shownDateKey] = date
        }
    }
}
