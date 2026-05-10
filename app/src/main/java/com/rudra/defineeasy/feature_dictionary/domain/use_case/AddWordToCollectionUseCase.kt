package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.repository.CollectionRepository
import javax.inject.Inject

class AddWordToCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    suspend operator fun invoke(collectionId: Int, word: String) {
        repository.addWordToCollection(collectionId, word)
    }
}
