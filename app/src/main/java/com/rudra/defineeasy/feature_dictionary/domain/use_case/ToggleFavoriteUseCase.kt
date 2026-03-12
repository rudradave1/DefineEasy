package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository

class ToggleFavoriteUseCase(
    private val repository: WordInfoRepository
) {
    suspend operator fun invoke(word: String) {
        repository.toggleFavorite(word)
    }
}
