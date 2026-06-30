package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.ViewModel
import com.rudra.defineeasy.preferences.StreakPreferences
import com.rudra.defineeasy.preferences.StreakState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class SearchStreakProvider @Inject constructor(
    private val streakPreferences: StreakPreferences
) : ViewModel() {
    fun streakState(): Flow<StreakState> = streakPreferences.streakState()
}
