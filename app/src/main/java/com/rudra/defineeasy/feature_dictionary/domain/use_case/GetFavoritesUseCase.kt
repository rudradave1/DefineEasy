package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow

class GetFavoritesUseCase(
    private val repository: WordInfoRepository
) {
    operator fun invoke(): Flow<List<WordInfo>> {
        return repository.getFavorites()
    }
}
