package com.rudra.defineeasy.feature_dictionary.presentation

import app.cash.turbine.test
import com.rudra.defineeasy.sampleWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetDueReviewCountUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetDueReviewWordsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.RateReviewedWordUseCase
import com.rudra.defineeasy.navigation.ReviewBadgeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val dueWordsFlow = MutableStateFlow<List<com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo>>(emptyList())
    private val getDueReviewWordsUseCase = mockk<GetDueReviewWordsUseCase>()
    private val rateReviewedWordUseCase = mockk<RateReviewedWordUseCase>()
    private val getDueReviewCountUseCase = mockk<GetDueReviewCountUseCase>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { getDueReviewWordsUseCase() } returns dueWordsFlow
        every { getDueReviewCountUseCase() } returns flowOf(2)
        coEvery { rateReviewedWordUseCase(any(), any()) } just runs
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dueWordsLoadCorrectlyOnInit() = runTest {
        dueWordsFlow.value = listOf(sampleWordInfo("alpha"), sampleWordInfo("beta"))

        val viewModel = ReviewViewModel(getDueReviewWordsUseCase, rateReviewedWordUseCase)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.dueWords.size)
        assertEquals("alpha", viewModel.uiState.value.currentWord?.word)
    }

    @Test
    fun ratingAgainUpdatesSm2ValuesCorrectly() = runTest {
        dueWordsFlow.value = listOf(sampleWordInfo("alpha"))
        val viewModel = ReviewViewModel(getDueReviewWordsUseCase, rateReviewedWordUseCase)
        advanceUntilIdle()

        viewModel.toggleAnswerVisibility()
        viewModel.rateCurrentWord(0)
        advanceUntilIdle()

        coVerify { rateReviewedWordUseCase("alpha", 0) }
    }

    @Test
    fun ratingGoodUpdatesSm2ValuesCorrectly() = runTest {
        dueWordsFlow.value = listOf(sampleWordInfo("alpha"))
        val viewModel = ReviewViewModel(getDueReviewWordsUseCase, rateReviewedWordUseCase)
        advanceUntilIdle()

        viewModel.toggleAnswerVisibility()
        viewModel.rateCurrentWord(3)
        advanceUntilIdle()

        coVerify { rateReviewedWordUseCase("alpha", 3) }
    }

    @Test
    fun allWordsRatedShowsEmptyState() = runTest {
        dueWordsFlow.value = listOf(sampleWordInfo("alpha"))
        val viewModel = ReviewViewModel(getDueReviewWordsUseCase, rateReviewedWordUseCase)
        advanceUntilIdle()

        viewModel.toggleAnswerVisibility()
        viewModel.rateCurrentWord(3)
        dueWordsFlow.value = emptyList()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.currentWord)
    }

    @Test
    fun badgeCountMatchesDueWordsCount() = runTest {
        val viewModel = ReviewBadgeViewModel(getDueReviewCountUseCase)

        viewModel.dueCount.test {
            assertEquals(0, awaitItem())
            assertEquals(2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
