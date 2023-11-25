package com.rudra.defineeasy.feature_dictionary.presentation

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo


data class WordInfoState(
    val wordInfoItems: List<WordInfo> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val isLoading: Boolean = false
)