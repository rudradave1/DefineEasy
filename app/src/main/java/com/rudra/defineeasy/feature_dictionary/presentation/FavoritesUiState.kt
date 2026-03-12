package com.rudra.defineeasy.feature_dictionary.presentation

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo

sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()
    data object Empty : FavoritesUiState()
    data class Success(val words: List<WordInfo>) : FavoritesUiState()
}
