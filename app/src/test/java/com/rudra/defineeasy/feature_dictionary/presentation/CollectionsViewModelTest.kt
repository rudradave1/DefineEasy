package com.rudra.defineeasy.feature_dictionary.presentation

import com.rudra.defineeasy.core.analytics.AnalyticsService
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetCollectionWordsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetCollectionsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetFavoritesUseCase
import com.rudra.defineeasy.feature_dictionary.domain.repository.CollectionRepository
import com.rudra.defineeasy.sampleWordInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val getFavoritesUseCase = mockk<GetFavoritesUseCase>()
    private val getCollectionsUseCase = mockk<GetCollectionsUseCase>()
    private val getCollectionWordsUseCase = mockk<GetCollectionWordsUseCase>()
    private val collectionRepository = mockk<CollectionRepository>()
    private val analyticsService = mockk<AnalyticsService>(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { getFavoritesUseCase() } returns flowOf(emptyList())
        every { collectionRepository.getCustomCollections() } returns flowOf(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadingStateOnInit() = runTest {
        coEvery { getCollectionsUseCase() } returns emptyList()

        val viewModel = CollectionsViewModel(
            getFavoritesUseCase, getCollectionsUseCase, getCollectionWordsUseCase,
            collectionRepository, analyticsService
        )

        assertInstanceOf(CollectionsUiState.Loading::class.java, viewModel.uiState.value)
    }

    @Test
    fun emptyStateWhenNoCollections() = runTest {
        coEvery { getCollectionsUseCase() } returns emptyList()

        val viewModel = CollectionsViewModel(
            getFavoritesUseCase, getCollectionsUseCase, getCollectionWordsUseCase,
            collectionRepository, analyticsService
        )
        advanceUntilIdle()

        assertInstanceOf(CollectionsUiState.Empty::class.java, viewModel.uiState.value)
    }

    @Test
    fun successStateWithStaticCollections() = runTest {
        val summaries = listOf(
            CollectionSummary(id = "gre", wordCount = 5),
            CollectionSummary(id = "sat", wordCount = 3),
        )
        coEvery { getCollectionsUseCase() } returns summaries
        coEvery { getCollectionWordsUseCase(collectionId = any()) } returns listOf(
            CollectionWord(word = "alpha"),
            CollectionWord(word = "beta"),
        )

        val viewModel = CollectionsViewModel(
            getFavoritesUseCase, getCollectionsUseCase, getCollectionWordsUseCase,
            collectionRepository, analyticsService
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertInstanceOf(CollectionsUiState.Success::class.java, state)
        val success = state as CollectionsUiState.Success
        assertEquals(2, success.collections.size)
        assertEquals(5, success.collections.first().wordCount)
    }

    @Test
    fun completionPercentageCalculatedCorrectly() = runTest {
        val summaries = listOf(
            CollectionSummary(id = "gre", wordCount = 4),
        )
        val words = listOf(
            CollectionWord(word = "alpha"),
            CollectionWord(word = "beta"),
            CollectionWord(word = "gamma"),
            CollectionWord(word = "delta"),
        )
        coEvery { getCollectionsUseCase() } returns summaries
        coEvery { getCollectionWordsUseCase(collectionId = "gre") } returns words
        // Mark half as favorited
        every { getFavoritesUseCase() } returns flowOf(
            listOf(sampleWordInfo("alpha"), sampleWordInfo("beta"))
        )

        val viewModel = CollectionsViewModel(
            getFavoritesUseCase, getCollectionsUseCase, getCollectionWordsUseCase,
            collectionRepository, analyticsService
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value as CollectionsUiState.Success
        assertEquals(50, state.collections.first().completionPercentage)
    }

    @Test
    fun zeroWordCountHandlesDivision() = runTest {
        val summaries = listOf(
            CollectionSummary(id = "empty", wordCount = 0),
        )
        coEvery { getCollectionsUseCase() } returns summaries
        coEvery { getCollectionWordsUseCase(collectionId = "empty") } returns emptyList()

        val viewModel = CollectionsViewModel(
            getFavoritesUseCase, getCollectionsUseCase, getCollectionWordsUseCase,
            collectionRepository, analyticsService
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value as CollectionsUiState.Success
        assertEquals(0, state.collections.first().completionPercentage)
    }

    @Test
    fun errorStateWhenUseCaseThrows() = runTest {
        coEvery { getCollectionsUseCase() } throws RuntimeException("API error")

        val viewModel = CollectionsViewModel(
            getFavoritesUseCase, getCollectionsUseCase, getCollectionWordsUseCase,
            collectionRepository, analyticsService
        )
        advanceUntilIdle()

        assertInstanceOf(CollectionsUiState.Error::class.java, viewModel.uiState.value)
        val error = viewModel.uiState.value as CollectionsUiState.Error
        assertTrue(error.message.contains("API error"))
    }

    @Test
    fun createCollectionDelegatesToRepositoryAndLogsAnalytics() = runTest {
        coEvery { getCollectionsUseCase() } returns emptyList()
        coEvery { collectionRepository.createCustomCollection("new") } coAnswers { 1L }

        val viewModel = CollectionsViewModel(
            getFavoritesUseCase, getCollectionsUseCase, getCollectionWordsUseCase,
            collectionRepository, analyticsService
        )
        advanceUntilIdle()

        viewModel.onEvent(CollectionsEvent.CreateCollection("new"))
        advanceUntilIdle()

        coVerify { collectionRepository.createCustomCollection("new") }
        verify { analyticsService.onCollectionCreated() }
    }

    @Test
    fun deleteCollectionDelegatesToRepository() = runTest {
        coEvery { getCollectionsUseCase() } returns emptyList()
        coEvery { collectionRepository.deleteCustomCollection(1) } coAnswers { }

        val viewModel = CollectionsViewModel(
            getFavoritesUseCase, getCollectionsUseCase, getCollectionWordsUseCase,
            collectionRepository, analyticsService
        )
        advanceUntilIdle()

        viewModel.onEvent(CollectionsEvent.DeleteCollection(1))
        advanceUntilIdle()

        coVerify { collectionRepository.deleteCustomCollection(1) }
    }
}
