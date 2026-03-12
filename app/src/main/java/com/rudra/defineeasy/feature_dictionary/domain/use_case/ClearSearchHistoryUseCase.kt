package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository

class ClearSearchHistoryUseCase(
    private val repository: WordInfoRepository
) {
    suspend operator fun invoke() {
        repository.clearSearchHistory()
    }
}
