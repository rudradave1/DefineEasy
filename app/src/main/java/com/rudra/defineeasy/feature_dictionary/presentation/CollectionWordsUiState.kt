package com.rudra.defineeasy.feature_dictionary.presentation

import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord

data class CollectionWordUiModel(
    val word: CollectionWord,
    val isInReview: Boolean
)

data class CollectionWordsUiState(
    val isLoading: Boolean = true,
    val collectionId: String = "",
    val words: List<CollectionWordUiModel> = emptyList(),
    val errorMessage: String? = null,
    val definitionStates: Map<String, WordDefinitionState> = emptyMap()
)
