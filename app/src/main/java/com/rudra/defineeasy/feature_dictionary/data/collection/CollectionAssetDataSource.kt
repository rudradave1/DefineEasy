package com.rudra.defineeasy.feature_dictionary.data.collection

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rudra.defineeasy.core.CrashReporter
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionIds
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionAssetDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val fileNames = mapOf(
        CollectionIds.UPSC to "collection_upsc.json",
        CollectionIds.CAT to "collection_cat.json",
        CollectionIds.BUSINESS to "collection_business.json",
        CollectionIds.CONFUSED to "collection_confused.json"
    )

    private val cache = mutableMapOf<String, List<CollectionWordDto>>()

    fun getCollectionWords(collectionId: String): List<CollectionWordDto> {
        return cache.getOrPut(collectionId) {
            val fileName = fileNames[collectionId]
                ?: error("Unknown collection id: $collectionId")
            try {
                context.assets.open(fileName).bufferedReader().use { reader ->
                    if (collectionId == CollectionIds.CONFUSED) {
                        val type = object : TypeToken<List<CollectionWordDto>>() {}.type
                        gson.fromJson<List<CollectionWordDto>>(reader, type)
                    } else {
                        val type = object : TypeToken<List<String>>() {}.type
                        val strings: List<String> = gson.fromJson(reader, type)
                        strings.map { CollectionWordDto(word = it) }
                    }
                }

            } catch (throwable: Throwable) {
                CrashReporter.logNonFatal(throwable)
                throw throwable
            }
        }
    }
}
