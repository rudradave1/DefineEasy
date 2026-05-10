package com.rudra.defineeasy.feature_dictionary.domain.repository

import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    suspend fun getCollections(): List<CollectionSummary>

    suspend fun getCollectionWords(collectionId: String): List<CollectionWord>

    // New methods for custom collections
    fun getCustomCollections(): Flow<List<CollectionSummary>>
    suspend fun createCustomCollection(name: String): Long
    suspend fun deleteCustomCollection(id: Int)
    suspend fun addWordToCollection(collectionId: Int, word: String)
    suspend fun removeWordFromCollection(collectionId: Int, word: String)
}
