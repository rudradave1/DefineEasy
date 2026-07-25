package com.rudra.defineeasy.feature_dictionary.presentation

import com.rudra.defineeasy.core.analytics.AnalyticsService
import com.rudra.defineeasy.feature_dictionary.domain.model.QuizQuestion
import com.rudra.defineeasy.feature_dictionary.domain.model.QuizState
import com.rudra.defineeasy.feature_dictionary.domain.quiz.QuizGenerator
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetDueReviewWordsUseCase
import com.rudra.defineeasy.sampleWordInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val quizGenerator = mockk<QuizGenerator>()
    private val getDueReviewWordsUseCase = mockk<GetDueReviewWordsUseCase>()
    private val analyticsService = mockk<AnalyticsService>(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadingStateOnInit() = runTest {
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returns emptyList()

        val viewModel = QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)

        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()
    }

    @Test
    fun quizLoadedWithQuestions() = runTest {
        val questions = listOf(
            QuizQuestion(word = "alpha", correctDefinition = "first", options = listOf("a", "b", "c", "d"), correctIndex = 0),
            QuizQuestion(word = "beta", correctDefinition = "second", options = listOf("a", "b", "c", "d"), correctIndex = 1),
        )
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returns questions

        val viewModel = QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.totalQuestions)
        assertEquals(0, viewModel.uiState.value.currentIndex)
        assertEquals("alpha", viewModel.uiState.value.currentQuestion?.word)
    }

    @Test
    fun emptyQuizWhenNoQuestionsGenerated() = runTest {
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returns emptyList()

        val viewModel = QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, viewModel.uiState.value.totalQuestions)
        assertNull(viewModel.uiState.value.currentQuestion)
    }

    @Test
    fun correctAnswerUpdatesScore() = runTest {
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returns listOf(
            QuizQuestion(word = "alpha", correctDefinition = "first", options = listOf("a", "b", "c", "d"), correctIndex = 1)
        )

        val viewModel = QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)
        advanceUntilIdle()

        viewModel.answerQuestion(1) // correct index
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.score)
        assertTrue(viewModel.uiState.value.isCorrect!!)
        assertEquals(1, viewModel.uiState.value.answeredIndex)
    }

    @Test
    fun wrongAnswerDoesNotIncrementScore() = runTest {
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returns listOf(
            QuizQuestion(word = "alpha", correctDefinition = "first", options = listOf("a", "b", "c", "d"), correctIndex = 2)
        )

        val viewModel = QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)
        advanceUntilIdle()

        viewModel.answerQuestion(0) // wrong index
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.score)
        assertFalse(viewModel.uiState.value.isCorrect!!)
    }

    @Test
    fun nextQuestionAdvancesIndex() = runTest {
        val questions = listOf(
            QuizQuestion(word = "alpha", correctDefinition = "first", options = listOf("a", "b", "c", "d"), correctIndex = 0),
            QuizQuestion(word = "beta", correctDefinition = "second", options = listOf("a", "b", "c", "d"), correctIndex = 1),
        )
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returns questions

        val viewModel = QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)
        advanceUntilIdle()

        viewModel.answerQuestion(0)
        viewModel.nextQuestion()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentIndex)
        assertEquals("beta", viewModel.uiState.value.currentQuestion?.word)
        assertNull(viewModel.uiState.value.answeredIndex)
        assertNull(viewModel.uiState.value.isCorrect)
    }

    @Test
    fun lastQuestionCompletesQuiz() = runTest {
        val questions = listOf(
            QuizQuestion(word = "alpha", correctDefinition = "first", options = listOf("a", "b", "c", "d"), correctIndex = 0)
        )
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returns questions

        val viewModel = QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)
        advanceUntilIdle()

        viewModel.answerQuestion(0)
        viewModel.nextQuestion()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isComplete)
    }

    @Test
    fun quizCompletionLogsAnalytics() = runTest {
        val questions = listOf(
            QuizQuestion(word = "alpha", correctDefinition = "first", options = listOf("a", "b", "c", "d"), correctIndex = 1)
        )
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returns questions

        val viewModel = QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)
        advanceUntilIdle()

        viewModel.answerQuestion(1)
        viewModel.nextQuestion()
        advanceUntilIdle()

        verify { analyticsService.onQuizCompleted(1, 1) }
    }

    @Test
    fun quizStartLogsAnalytics() = runTest {
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returns emptyList()

        QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)
        advanceUntilIdle()

        verify { analyticsService.onQuizStarted() }
    }

    @Test
    fun restartQuizResetsStateAndGeneratesNewQuestions() = runTest {
        val questions1 = listOf(
            QuizQuestion(word = "alpha", correctDefinition = "first", options = listOf("a", "b", "c", "d"), correctIndex = 0)
        )
        val questions2 = listOf(
            QuizQuestion(word = "beta", correctDefinition = "second", options = listOf("a", "b", "c", "d"), correctIndex = 1)
        )
        every { getDueReviewWordsUseCase() } returns flowOf(emptyList())
        every { quizGenerator.generateQuestions(any(), any(), any(), any()) } returnsMany listOf(questions1, questions2)

        val viewModel = QuizViewModel(quizGenerator, getDueReviewWordsUseCase, analyticsService)
        advanceUntilIdle()

        // Complete first quiz
        viewModel.answerQuestion(0)
        viewModel.nextQuestion()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isComplete)

        // Restart
        viewModel.restartQuiz()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isComplete)
        assertEquals(0, viewModel.uiState.value.score)
        assertEquals(0, viewModel.uiState.value.currentIndex)
        assertEquals("beta", viewModel.uiState.value.currentQuestion?.word)
    }
}
