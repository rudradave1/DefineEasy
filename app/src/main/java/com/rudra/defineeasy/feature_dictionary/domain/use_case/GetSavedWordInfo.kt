package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository

class GetSavedWordInfo(
    private val repository: WordInfoRepository
) {
    suspend operator fun invoke(word: String): WordInfo? {
        return repository.getSavedWordInfo(word)
    }
}
