package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary
import com.rudra.defineeasy.feature_dictionary.domain.repository.CollectionRepository
import javax.inject.Inject

class GetCollectionsUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    suspend operator fun invoke(): List<CollectionSummary> {
        return repository.getCollections()
    }
}
