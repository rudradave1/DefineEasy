package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import javax.inject.Inject

class SaveWordInfoUseCase @Inject constructor(
    private val repository: WordInfoRepository
) {
    suspend operator fun invoke(wordInfo: WordInfo) {
        repository.saveWordInfo(wordInfo)
    }
}
