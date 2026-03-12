package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetFavoritesUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        getFavoritesUseCase()
            .onEach { words ->
                _uiState.value = if (words.isEmpty()) {
                    FavoritesUiState.Empty
                } else {
                    FavoritesUiState.Success(words)
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite(word: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(word)
        }
    }
}
