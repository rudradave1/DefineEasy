package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord
import com.rudra.defineeasy.feature_dictionary.domain.repository.CollectionRepository
import javax.inject.Inject

class GetCollectionWordsUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    suspend operator fun invoke(collectionId: String): List<CollectionWord> {
        return repository.getCollectionWords(collectionId)
    }
}
