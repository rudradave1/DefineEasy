package com.rudra.defineeasy.feature_dictionary.presentation

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo


data class WordInfoState(
    val wordInfoItems: List<WordInfo> = emptyList(),
    val isSearchHistoryVisible: Boolean = false,
    val searchHistory: Set<String> = emptySet(),
    val isLoading: Boolean = false
)