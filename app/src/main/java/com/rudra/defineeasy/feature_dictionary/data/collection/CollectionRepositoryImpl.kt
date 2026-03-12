package com.rudra.defineeasy.feature_dictionary.data.collection

import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionIds
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord
import com.rudra.defineeasy.feature_dictionary.domain.repository.CollectionRepository
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val assetDataSource: CollectionAssetDataSource
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
        return assetDataSource.getCollectionWords(collectionId).map(CollectionWordDto::toCollectionWord)
    }
}
