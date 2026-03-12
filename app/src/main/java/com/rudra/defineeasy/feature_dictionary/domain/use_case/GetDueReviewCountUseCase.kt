package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow

class GetDueReviewCountUseCase(
    private val repository: WordInfoRepository
) {
    operator fun invoke(): Flow<Int> {
        return repository.getDueReviewCount()
    }
}
