package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow

class IsWordFavoritedUseCase(
    private val repository: WordInfoRepository
) {
    operator fun invoke(word: String): Flow<Boolean> {
        return repository.isWordFavorited(word)
    }
}
