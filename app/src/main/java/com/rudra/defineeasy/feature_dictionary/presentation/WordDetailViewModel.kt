package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.core.util.Resource
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.IsWordFavoritedUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetSavedWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWordInfo: GetWordInfo,
    private val getSavedWordInfo: GetSavedWordInfo,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isWordFavoritedUseCase: IsWordFavoritedUseCase
) : ViewModel() {

    private val selectedWord = savedStateHandle.get<String>("word").orEmpty()

    private val _state = androidx.compose.runtime.mutableStateOf(WordDetailState())
    val state: androidx.compose.runtime.State<WordDetailState> = _state

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    private var loadJob: Job? = null

    init {
        observeFavoriteState()
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = getWordInfo(selectedWord)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            wordInfo = result.data?.findExact(selectedWord) ?: _state.value.wordInfo,
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            wordInfo = result.data?.findExact(selectedWord),
                            isLoading = false,
                            errorMessage = null
                        )
                    }

                    is Resource.Error -> {
                        val fallbackWordInfo = result.data?.findExact(selectedWord)
                            ?: getSavedWordInfo(selectedWord)
                        _state.value = _state.value.copy(
                            wordInfo = fallbackWordInfo,
                            isLoading = false,
                            errorMessage = result.message
                        )
                        _eventFlow.emit(UIEvent.ShowSnackbar(result.message ?: "Unknown error"))
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite() {
        val word = _state.value.wordInfo?.word ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(word)
        }
    }

    sealed class UIEvent {
        data class ShowSnackbar(val message: String) : UIEvent()
    }

    private fun List<WordInfo>.findExact(word: String): WordInfo? {
        return firstOrNull { it.word.equals(word, ignoreCase = true) }
            ?: firstOrNull()
    }

    private fun observeFavoriteState() {
        isWordFavoritedUseCase(selectedWord)
            .onEach { isFavorited ->
                val currentWordInfo = _state.value.wordInfo ?: getSavedWordInfo(selectedWord)
                _state.value = _state.value.copy(
                    wordInfo = currentWordInfo?.copy(isFavorited = isFavorited)
                )
            }
            .launchIn(viewModelScope)
    }
}
