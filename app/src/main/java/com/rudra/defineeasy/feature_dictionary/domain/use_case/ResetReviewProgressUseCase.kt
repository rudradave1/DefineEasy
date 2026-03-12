package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import javax.inject.Inject

class ResetReviewProgressUseCase @Inject constructor(
    private val repository: WordInfoRepository
) {
    suspend operator fun invoke() {
        repository.resetReviewProgress()
    }
}
