package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow

class GetDueReviewWordsUseCase(
    private val repository: WordInfoRepository
) {
    operator fun invoke(): Flow<List<WordInfo>> {
        return repository.getDueReviewWords()
    }
}
