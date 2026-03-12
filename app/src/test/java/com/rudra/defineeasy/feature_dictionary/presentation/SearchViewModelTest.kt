package com.rudra.defineeasy.feature_dictionary.presentation

import app.cash.turbine.test
import com.rudra.defineeasy.sampleWordInfo
import com.rudra.defineeasy.core.util.Resource
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ClearSearchHistoryUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.DeleteSearchHistoryItemUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetSearchHistory
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetWordInfo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val getWordInfo = mockk<GetWordInfo>()
    private val getSearchHistory = mockk<GetSearchHistory>()
    private val deleteSearchHistoryItemUseCase = mockk<DeleteSearchHistoryItemUseCase>()
    private val clearSearchHistoryUseCase = mockk<ClearSearchHistoryUseCase>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { getSearchHistory() } returns emptyList()
        coEvery { deleteSearchHistoryItemUseCase(any()) } just runs
        coEvery { clearSearchHistoryUseCase() } just runs
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsEmpty() {
        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.wordInfoItems.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun searchQueryEmitsLoadingThenSuccess() = runTest {
        every { getWordInfo("word") } returns flow {
            emit(Resource.Loading())
            emit(Resource.Success(listOf(sampleWordInfo("word"))))
        }
        val viewModel = createViewModel()

        viewModel.onQueryChanged("word")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertEquals(listOf("word"), viewModel.state.value.wordInfoItems.map { it.word })
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun searchQueryEmitsLoadingThenError() = runTest {
        every { getWordInfo("word") } returns flow {
            emit(Resource.Loading())
            emit(Resource.Error("network", emptyList()))
        }
        val viewModel = createViewModel()

        viewModel.eventFlow.test {
            viewModel.onQueryChanged("word")
            advanceTimeBy(500)
            advanceUntilIdle()
            assertEquals("network", (awaitItem() as WordInfoViewModel.UIEvent.ShowSnackbar).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearingQueryClearsResultsState() = runTest {
        every { getWordInfo("word") } returns flow {
            emit(Resource.Success(listOf(sampleWordInfo("word"))))
        }
        val viewModel = createViewModel()

        viewModel.onQueryChanged("word")
        advanceTimeBy(500)
        advanceUntilIdle()
        viewModel.onQueryChanged("")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.wordInfoItems.isEmpty())
        assertFalse(viewModel.state.value.hasSearched)
    }

    @Test
    fun recentSearchesLoadOnFocus() = runTest {
        coEvery { getSearchHistory() } returns listOf("alpha", "beta")
        val viewModel = createViewModel()

        viewModel.onSearchFieldFocusChanged(true)
        advanceUntilIdle()

        assertEquals(listOf("alpha", "beta"), viewModel.state.value.searchHistory)
    }

    private fun createViewModel() = WordInfoViewModel(
        getWordInfo = getWordInfo,
        getSearchHistory = getSearchHistory,
        deleteSearchHistoryItemUseCase = deleteSearchHistoryItemUseCase,
        clearSearchHistoryUseCase = clearSearchHistoryUseCase
    )
}
