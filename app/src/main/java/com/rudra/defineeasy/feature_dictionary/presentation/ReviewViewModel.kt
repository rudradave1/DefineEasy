package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetDueReviewWordsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.RateReviewedWordUseCase
import com.rudra.defineeasy.preferences.ReviewPromptPreferences
import com.rudra.defineeasy.preferences.StreakPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class ReviewViewModel @Inject constructor(
    getDueReviewWordsUseCase: GetDueReviewWordsUseCase,
    private val rateReviewedWordUseCase: RateReviewedWordUseCase,
    private val streakPreferences: StreakPreferences,
    private val reviewPromptPreferences: ReviewPromptPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        getDueReviewWordsUseCase()
            .onEach { dueWords ->
                val previousState = _uiState.value
                val currentWordIndex = previousState.currentWord?.word?.let { currentWord ->
                    dueWords.indexOfFirst { it.word == currentWord }
                } ?: -1

                val nextIndex = when {
                    dueWords.isEmpty() -> 0
                    currentWordIndex >= 0 -> currentWordIndex
                    previousState.currentIndex > dueWords.lastIndex -> dueWords.lastIndex
                    else -> previousState.currentIndex.coerceAtLeast(0)
                }

                _uiState.value = previousState.copy(
                    dueWords = dueWords,
                    currentIndex = nextIndex,
                    isLoading = false,
                    isAnswerVisible = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun toggleAnswerVisibility() {
        _uiState.value = _uiState.value.copy(
            isAnswerVisible = !_uiState.value.isAnswerVisible
        )
    }

    fun rateCurrentWord(quality: Int) {
        val currentWord = _uiState.value.currentWord ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                completedCount = _uiState.value.completedCount + 1,
                isAnswerVisible = false
            )
            rateReviewedWordUseCase(currentWord.word, quality)
            streakPreferences.recordReview(LocalDate.now().toEpochDay())
            reviewPromptPreferences.incrementReviewCount()
        }
    }
}
