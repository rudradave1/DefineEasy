package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.SavedStateHandle
import com.rudra.defineeasy.core.analytics.AnalyticsService
import com.rudra.defineeasy.core.util.Resource
import com.rudra.defineeasy.feature_dictionary.domain.use_case.AddWordToCollectionUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetCustomCollectionsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetSavedWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.IsWordFavoritedUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ToggleFavoriteUseCase
import com.rudra.defineeasy.sampleWordInfo
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WordDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val savedStateHandle = mockk<SavedStateHandle>()
    private val getWordInfo = mockk<GetWordInfo>()
    private val getSavedWordInfo = mockk<GetSavedWordInfo>(relaxed = true)
    private val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>(relaxed = true)
    private val isWordFavoritedUseCase = mockk<IsWordFavoritedUseCase>()
    private val getCustomCollectionsUseCase = mockk<GetCustomCollectionsUseCase>()
    private val addWordToCollectionUseCase = mockk<AddWordToCollectionUseCase>(relaxed = true)
    private val analyticsService = mockk<AnalyticsService>(relaxed = true)

    private val word = "testword"

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { savedStateHandle.get<String>("word") } returns word
        every { isWordFavoritedUseCase(any()) } returns flowOf(false)
        every { getCustomCollectionsUseCase() } returns flowOf(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsWordInfoOnInit() = runTest(dispatcher) {
        val wordInfo = sampleWordInfo(word)
        every { getWordInfo(word) } returns flowOf(Resource.Success(listOf(wordInfo)))

        val viewModel = WordDetailViewModel(
            savedStateHandle, getWordInfo, getSavedWordInfo,
            toggleFavoriteUseCase, isWordFavoritedUseCase,
            getCustomCollectionsUseCase, addWordToCollectionUseCase, analyticsService
        )
        advanceUntilIdle()

        assertEquals(wordInfo, viewModel.state.value.wordInfo)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun loadingStateThenSuccessAfterAdvance() = runTest(dispatcher) {
        val wordInfo = sampleWordInfo(word)
        every { getWordInfo(word) } returns flowOf(
            Resource.Loading(listOf(wordInfo)),
            Resource.Success(listOf(wordInfo))
        )

        val viewModel = WordDetailViewModel(
            savedStateHandle, getWordInfo, getSavedWordInfo,
            toggleFavoriteUseCase, isWordFavoritedUseCase,
            getCustomCollectionsUseCase, addWordToCollectionUseCase, analyticsService
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(wordInfo, viewModel.state.value.wordInfo)
    }

    @Test
    fun errorStateShowsMessageAndFallsBackToSaved() = runTest(dispatcher) {
        val savedWordInfo = sampleWordInfo(word)
        every { getWordInfo(word) } returns flowOf(
            Resource.Error(message = "Network error", data = null)
        )
        coEvery { getSavedWordInfo(word) } returns savedWordInfo

        val viewModel = WordDetailViewModel(
            savedStateHandle, getWordInfo, getSavedWordInfo,
            toggleFavoriteUseCase, isWordFavoritedUseCase,
            getCustomCollectionsUseCase, addWordToCollectionUseCase, analyticsService
        )
        advanceUntilIdle()

        assertEquals("Network error", viewModel.state.value.errorMessage)
        assertNotNull(viewModel.state.value.wordInfo)
    }

    @Test
    fun toggleFavoriteCallsUseCaseAndAnalytics() = runTest(dispatcher) {
        val wordInfo = sampleWordInfo(word)
        every { getWordInfo(word) } returns flowOf(Resource.Success(listOf(wordInfo)))

        val viewModel = WordDetailViewModel(
            savedStateHandle, getWordInfo, getSavedWordInfo,
            toggleFavoriteUseCase, isWordFavoritedUseCase,
            getCustomCollectionsUseCase, addWordToCollectionUseCase, analyticsService
        )
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        coVerify { toggleFavoriteUseCase(word) }
        verify { analyticsService.onFavoriteToggled(word, true) }
    }

    @Test
    fun logShareCallsAnalytics() = runTest(dispatcher) {
        val wordInfo = sampleWordInfo(word)
        every { getWordInfo(word) } returns flowOf(Resource.Success(listOf(wordInfo)))

        val viewModel = WordDetailViewModel(
            savedStateHandle, getWordInfo, getSavedWordInfo,
            toggleFavoriteUseCase, isWordFavoritedUseCase,
            getCustomCollectionsUseCase, addWordToCollectionUseCase, analyticsService
        )
        advanceUntilIdle()

        viewModel.logShare()
        verify { analyticsService.onWordShared(word) }
    }

    @Test
    fun addWordToCollectionDelegatesCorrectly() = runTest(dispatcher) {
        val wordInfo = sampleWordInfo(word)
        val collectionId = 1
        every { getWordInfo(word) } returns flowOf(Resource.Success(listOf(wordInfo)))

        val viewModel = WordDetailViewModel(
            savedStateHandle, getWordInfo, getSavedWordInfo,
            toggleFavoriteUseCase, isWordFavoritedUseCase,
            getCustomCollectionsUseCase, addWordToCollectionUseCase, analyticsService
        )
        advanceUntilIdle()

        viewModel.addWordToCollection(collectionId)
        advanceUntilIdle()

        coVerify { addWordToCollectionUseCase(collectionId, word) }
    }

    @Test
    fun customCollectionsLoadedOnInit() = runTest(dispatcher) {
        every { getWordInfo(word) } returns flowOf(Resource.Success(listOf(sampleWordInfo(word))))

        val viewModel = WordDetailViewModel(
            savedStateHandle, getWordInfo, getSavedWordInfo,
            toggleFavoriteUseCase, isWordFavoritedUseCase,
            getCustomCollectionsUseCase, addWordToCollectionUseCase, analyticsService
        )
        advanceUntilIdle()

        verify { getCustomCollectionsUseCase() }
    }

    @Test
    fun noCrashWhenWordArgMissing() = runTest(dispatcher) {
        every { savedStateHandle.get<String>("word") } returns null
        every { getWordInfo("") } returns flowOf(Resource.Success(emptyList()))

        val viewModel = WordDetailViewModel(
            savedStateHandle, getWordInfo, getSavedWordInfo,
            toggleFavoriteUseCase, isWordFavoritedUseCase,
            getCustomCollectionsUseCase, addWordToCollectionUseCase, analyticsService
        )
        advanceUntilIdle()

        assertNull(viewModel.state.value.wordInfo)
    }
}
