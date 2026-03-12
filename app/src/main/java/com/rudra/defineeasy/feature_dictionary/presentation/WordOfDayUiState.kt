package com.rudra.defineeasy.feature_dictionary.presentation

import com.rudra.defineeasy.feature_dictionary.domain.model.WordOfDay

data class WordOfDayUiState(
    val wordOfDay: WordOfDay? = null,
    val isVisible: Boolean = false,
    val isFavorited: Boolean = false
)
