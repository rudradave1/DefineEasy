package com.rudra.defineeasy.feature_dictionary.presentation

import app.cash.turbine.test
import com.rudra.defineeasy.feature_dictionary.data.local.ReviewHistoryDao
import com.rudra.defineeasy.feature_dictionary.data.local.entity.ReviewHistoryEntity
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import com.rudra.defineeasy.sampleWordInfo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<WordInfoRepository>()
    private val reviewHistoryDao = mockk<ReviewHistoryDao>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsAsLoadingThenSuccessWithEmptyData() = runTest(dispatcher) {
        every { repository.getFavorites() } returns flowOf(emptyList())
        every { reviewHistoryDao.getReviewHistory() } returns flowOf(emptyList())

        val viewModel = ProgressViewModel(repository, reviewHistoryDao)

        viewModel.uiState.test {
            assertEquals(ProgressUiState.Loading, awaitItem())
            val item = awaitItem()
            require(item is ProgressUiState.Success)
            assertEquals(0, item.masteredCount)
            assertEquals(0, item.learningCount)
            assertEquals(emptyList<String>(), item.reviewHistory)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun favoritesCountedCorrectly() = runTest(dispatcher) {
        val favorites = listOf(
            sampleWordInfo("alpha").copy(repetitions = 5),  // mastered
            sampleWordInfo("beta").copy(repetitions = 4),   // mastered
            sampleWordInfo("gamma").copy(repetitions = 2),  // learning
            sampleWordInfo("delta").copy(repetitions = 0),  // not counted
        )
        every { repository.getFavorites() } returns flowOf(favorites)
        every { reviewHistoryDao.getReviewHistory() } returns flowOf(emptyList())

        val viewModel = ProgressViewModel(repository, reviewHistoryDao)

        viewModel.uiState.test {
            awaitItem() // Loading
            val item = awaitItem()
            require(item is ProgressUiState.Success)
            assertEquals(2, item.masteredCount)
            assertEquals(1, item.learningCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reviewHistoryMapsWordOnly() = runTest(dispatcher) {
        every { repository.getFavorites() } returns flowOf(emptyList())
        val history = listOf(
            ReviewHistoryEntity(word = "alpha", rating = 3, timestamp = 100L),
            ReviewHistoryEntity(word = "beta", rating = 5, timestamp = 200L),
        )
        every { reviewHistoryDao.getReviewHistory() } returns flowOf(history)

        val viewModel = ProgressViewModel(repository, reviewHistoryDao)

        viewModel.uiState.test {
            awaitItem() // Loading
            val item = awaitItem()
            require(item is ProgressUiState.Success)
            assertEquals(listOf("alpha", "beta"), item.reviewHistory)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reviewHistoryLimitedToRecent10() = runTest(dispatcher) {
        every { repository.getFavorites() } returns flowOf(emptyList())
        val history = (1..15).map { ReviewHistoryEntity(word = "word$it", rating = 3, timestamp = it.toLong()) }
        every { reviewHistoryDao.getReviewHistory() } returns flowOf(history)

        val viewModel = ProgressViewModel(repository, reviewHistoryDao)

        viewModel.uiState.test {
            awaitItem() // Loading
            val item = awaitItem()
            require(item is ProgressUiState.Success)
            assertEquals(10, item.reviewHistory.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
