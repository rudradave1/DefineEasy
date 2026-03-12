package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetDueReviewWordsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.RateReviewedWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val rateReviewedWordUseCase: RateReviewedWordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        getDueReviewWordsUseCase()
            .onEach { dueWords ->
                val nextIndex = _uiState.value.currentIndex.coerceAtMost(dueWords.lastIndex.coerceAtLeast(0))
                _uiState.value = _uiState.value.copy(
                    dueWords = dueWords,
                    currentIndex = if (dueWords.isEmpty()) 0 else nextIndex,
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
            rateReviewedWordUseCase(currentWord.word, quality)
            _uiState.value = _uiState.value.copy(
                currentIndex = (_uiState.value.currentIndex + 1).coerceAtMost(_uiState.value.dueWords.size),
                isAnswerVisible = false
            )
        }
    }
}
