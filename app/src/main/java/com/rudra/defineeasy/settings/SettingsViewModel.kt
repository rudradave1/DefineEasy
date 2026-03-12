package com.rudra.defineeasy.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ClearAllFavoritesUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ClearSearchHistoryUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ResetReviewProgressUseCase
import com.rudra.defineeasy.notifications.ReviewReminderScheduler
import com.rudra.defineeasy.preferences.ReminderPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    reminderPreferences: ReminderPreferences,
    private val reminderSettingsStore: ReminderPreferences,
    private val reviewReminderScheduler: ReviewReminderScheduler,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
    private val clearAllFavoritesUseCase: ClearAllFavoritesUseCase,
    private val resetReviewProgressUseCase: ResetReviewProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        reminderPreferences.reminderSettings()
            .onEach { settings ->
                _uiState.value = SettingsUiState(
                    reminderEnabled = settings.enabled,
                    reminderHour = settings.hour,
                    reminderMinute = settings.minute,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            reminderSettingsStore.setReminderEnabled(enabled)
            if (enabled) {
                val state = uiState.value
                reviewReminderScheduler.reschedule(state.reminderHour, state.reminderMinute)
            } else {
                reviewReminderScheduler.cancel()
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            reminderSettingsStore.setReminderTime(hour, minute)
            if (uiState.value.reminderEnabled) {
                reviewReminderScheduler.reschedule(hour, minute)
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            clearSearchHistoryUseCase()
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            clearAllFavoritesUseCase()
        }
    }

    fun resetReviewProgress() {
        viewModelScope.launch {
            resetReviewProgressUseCase()
        }
    }
}
