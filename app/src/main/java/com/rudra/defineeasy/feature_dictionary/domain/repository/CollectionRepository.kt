package com.rudra.defineeasy.feature_dictionary.domain.repository

import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord

interface CollectionRepository {
    suspend fun getCollections(): List<CollectionSummary>

    suspend fun getCollectionWords(collectionId: String): List<CollectionWord>
}
