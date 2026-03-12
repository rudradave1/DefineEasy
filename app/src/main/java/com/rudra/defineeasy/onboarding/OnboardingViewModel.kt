package com.rudra.defineeasy.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.preferences.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        onboardingPreferences.isOnboardingCompleted()
            .onEach { isCompleted ->
                _uiState.value = OnboardingUiState(
                    isLoading = false,
                    isCompleted = isCompleted
                )
            }
            .launchIn(viewModelScope)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted(true)
        }
    }
}
