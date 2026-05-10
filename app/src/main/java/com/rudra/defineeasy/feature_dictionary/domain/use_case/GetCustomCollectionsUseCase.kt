package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary
import com.rudra.defineeasy.feature_dictionary.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCustomCollectionsUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    operator fun invoke(): Flow<List<CollectionSummary>> {
        return repository.getCustomCollections()
    }
}
