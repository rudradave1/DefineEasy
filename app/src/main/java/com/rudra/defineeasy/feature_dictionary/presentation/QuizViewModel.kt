package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.core.analytics.AnalyticsService
import com.rudra.defineeasy.feature_dictionary.domain.model.QuizState
import com.rudra.defineeasy.feature_dictionary.domain.quiz.QuizGenerator
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetDueReviewWordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizGenerator: QuizGenerator,
    private val getDueReviewWordsUseCase: GetDueReviewWordsUseCase,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizState())
    val uiState: StateFlow<QuizState> = _uiState.asStateFlow()

    init {
        loadQuiz()
    }

    private fun loadQuiz() {
        _uiState.value = QuizState(isLoading = true)
        analyticsService.onQuizStarted()

        getDueReviewWordsUseCase()
            .onEach { dueWords ->
                val allWords = dueWords
                val questions = quizGenerator.generateQuestions(
                    dueWords = dueWords,
                    recentSearches = emptyList(),
                    allWords = allWords,
                    questionCount = 5
                )
                _uiState.value = QuizState(
                    questions = questions,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun answerQuestion(selectedIndex: Int) {
        val current = _uiState.value.currentQuestion ?: return
        val isCorrect = selectedIndex == current.correctIndex

        _uiState.value = _uiState.value.copy(
            answeredIndex = selectedIndex,
            isCorrect = isCorrect,
            score = if (isCorrect) _uiState.value.score + 1 else _uiState.value.score
        )
    }

    fun nextQuestion() {
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex >= _uiState.value.totalQuestions) {
            _uiState.value = _uiState.value.copy(isComplete = true)
            analyticsService.onQuizCompleted(_uiState.value.score, _uiState.value.totalQuestions)
        } else {
            _uiState.value = _uiState.value.copy(
                currentIndex = nextIndex,
                answeredIndex = null,
                isCorrect = null
            )
        }
    }

    fun restartQuiz() {
        loadQuiz()
    }
}
