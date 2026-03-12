package com.rudra.defineeasy.feature_dictionary.presentation


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.core.util.Resource
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ClearSearchHistoryUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.DeleteSearchHistoryItemUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetSearchHistory
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetWordInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordInfoViewModel @Inject constructor(
    private val getWordInfo: GetWordInfo,
    private val getSearchHistory: GetSearchHistory,
    private val deleteSearchHistoryItemUseCase: DeleteSearchHistoryItemUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _state = mutableStateOf(WordInfoState())
    val state: State<WordInfoState> = _state

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.value = state.value.copy(
                wordInfoItems = emptyList(),
                isLoading = false,
                hasSearched = false
            )
            loadSearchHistory()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500L)
            executeSearch(query)
        }
    }

    fun submitSearch() {
        val query = searchQuery.value.trim()
        if (query.isBlank()) {
            _state.value = state.value.copy(
                wordInfoItems = emptyList(),
                isLoading = false,
                hasSearched = false
            )
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            executeSearch(query)
        }
    }

    fun onSearchFieldFocusChanged(isFocused: Boolean) {
        _state.value = state.value.copy(isSearchFieldFocused = isFocused)
        if (isFocused && searchQuery.value.isBlank()) {
            loadSearchHistory()
        }
    }

    fun onSearchHistoryItemSelected(word: String) {
        _searchQuery.value = word
        submitSearch()
    }

    fun deleteSearchHistoryItem(word: String) {
        viewModelScope.launch {
            deleteSearchHistoryItemUseCase(word)
            loadSearchHistory()
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            clearSearchHistoryUseCase()
            loadSearchHistory()
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            _state.value = state.value.copy(
                searchHistory = getSearchHistory()
            )
        }
    }

    private fun executeSearch(query: String) {
        getWordInfo(query)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            wordInfoItems = result.data ?: emptyList(),
                            isLoading = false,
                            hasSearched = true
                        )
                    }

                    is Resource.Error -> {
                        _state.value = state.value.copy(
                            wordInfoItems = result.data ?: emptyList(),
                            isLoading = false,
                            hasSearched = true
                        )
                        _eventFlow.emit(
                            UIEvent.ShowSnackbar(
                                result.message ?: "Unknown error"
                            )
                        )
                    }

                    is Resource.Loading -> {
                        _state.value = state.value.copy(
                            wordInfoItems = result.data ?: emptyList(),
                            isLoading = true,
                            hasSearched = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    sealed class UIEvent {
        data class ShowSnackbar(val message: String) : UIEvent()
    }
}
