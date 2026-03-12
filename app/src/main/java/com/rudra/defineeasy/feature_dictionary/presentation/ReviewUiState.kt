package com.rudra.defineeasy.feature_dictionary.presentation

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo

data class ReviewUiState(
    val dueWords: List<WordInfo> = emptyList(),
    val currentIndex: Int = 0,
    val isAnswerVisible: Boolean = false,
    val isLoading: Boolean = true
) {
    val currentWord: WordInfo?
        get() = dueWords.getOrNull(currentIndex)

    val completedCount: Int
        get() = currentIndex.coerceAtMost(dueWords.size)
}
