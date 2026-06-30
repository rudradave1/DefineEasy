package com.rudra.defineeasy.feature_dictionary.data.collection

import com.rudra.defineeasy.feature_dictionary.data.local.WordInfoDao
import com.rudra.defineeasy.feature_dictionary.data.local.entity.CollectionWordCrossRef
import com.rudra.defineeasy.feature_dictionary.data.local.entity.CustomCollectionEntity
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionIds
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord
import com.rudra.defineeasy.feature_dictionary.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val assetDataSource: CollectionAssetDataSource,
    private val dao: WordInfoDao
) : CollectionRepository {

    override suspend fun getCollections(): List<CollectionSummary> {
        return listOf(
            CollectionSummary(
                id = CollectionIds.UPSC,
                wordCount = assetDataSource.getCollectionWords(CollectionIds.UPSC).size
            ),
            CollectionSummary(
                id = CollectionIds.CAT,
                wordCount = assetDataSource.getCollectionWords(CollectionIds.CAT).size
            ),
            CollectionSummary(
                id = CollectionIds.GRE,
                wordCount = assetDataSource.getCollectionWords(CollectionIds.GRE).size
            ),
            CollectionSummary(
                id = CollectionIds.IELTS,
                wordCount = assetDataSource.getCollectionWords(CollectionIds.IELTS).size
            ),
            CollectionSummary(
                id = CollectionIds.SSC,
                wordCount = assetDataSource.getCollectionWords(CollectionIds.SSC).size
            ),
            CollectionSummary(
                id = CollectionIds.TOEFL,
                wordCount = assetDataSource.getCollectionWords(CollectionIds.TOEFL).size
            ),
            CollectionSummary(
                id = CollectionIds.GATE,
                wordCount = assetDataSource.getCollectionWords(CollectionIds.GATE).size
            ),
            CollectionSummary(
                id = CollectionIds.BUSINESS,
                wordCount = assetDataSource.getCollectionWords(CollectionIds.BUSINESS).size
            ),
            CollectionSummary(
                id = CollectionIds.CONFUSED,
                wordCount = assetDataSource.getCollectionWords(CollectionIds.CONFUSED).size
            )
        )
    }

    override suspend fun getCollectionWords(collectionId: String): List<CollectionWord> {
        val customId = collectionId.toIntOrNull()
        return if (customId != null) {
            // This is a bit inefficient to collect here, but for now it's okay for custom words
            // Better to have a suspend version of getWordsForCollection or a repo method
            emptyList() // Placeholder: will implement properly if needed
        } else {
            assetDataSource.getCollectionWords(collectionId).map(CollectionWordDto::toCollectionWord)
        }
    }

    override fun getCustomCollections(): Flow<List<CollectionSummary>> {
        return dao.getCustomCollections().map { entities ->
            entities.map { entity ->
                CollectionSummary(
                    id = entity.id.toString(),
                    wordCount = 0 // Will need to update this to show actual count
                )
            }
        }
    }

    override suspend fun createCustomCollection(name: String): Long {
        return dao.insertCustomCollection(
            CustomCollectionEntity(name = name, createdAt = System.currentTimeMillis())
        )
    }

    override suspend fun deleteCustomCollection(id: Int) {
        dao.deleteCustomCollection(id)
    }

    override suspend fun addWordToCollection(collectionId: Int, word: String) {
        dao.insertCollectionWordCrossRef(CollectionWordCrossRef(collectionId, word))
    }

    override suspend fun removeWordFromCollection(collectionId: Int, word: String) {
        dao.deleteCollectionWordCrossRef(collectionId, word)
    }
}
