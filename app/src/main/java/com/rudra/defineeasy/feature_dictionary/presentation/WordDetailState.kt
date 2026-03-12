package com.rudra.defineeasy.feature_dictionary.presentation

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo

data class WordDetailState(
    val wordInfo: WordInfo? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
