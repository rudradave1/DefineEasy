package com.rudra.defineeasy.feature_dictionary.presentation

data class CollectionCardUiModel(
    val id: String,
    val wordCount: Int,
    val completionPercentage: Int
)

sealed interface CollectionsUiState {
    data object Loading : CollectionsUiState
    data object Empty : CollectionsUiState
    data class Error(val message: String) : CollectionsUiState
    data class Success(val collections: List<CollectionCardUiModel>) : CollectionsUiState
}
