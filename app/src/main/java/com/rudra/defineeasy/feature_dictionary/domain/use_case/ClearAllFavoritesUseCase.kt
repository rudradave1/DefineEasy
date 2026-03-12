package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import javax.inject.Inject

class ClearAllFavoritesUseCase @Inject constructor(
    private val repository: WordInfoRepository
) {
    suspend operator fun invoke() {
        repository.clearAllFavorites()
    }
}
