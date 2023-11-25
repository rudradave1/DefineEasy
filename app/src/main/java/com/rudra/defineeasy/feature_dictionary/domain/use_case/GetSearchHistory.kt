package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository

class GetSearchHistory(
    private val repository: WordInfoRepository
) {

    suspend operator fun invoke(): List<String> {
        return repository.getSearchHistory()
    }

}