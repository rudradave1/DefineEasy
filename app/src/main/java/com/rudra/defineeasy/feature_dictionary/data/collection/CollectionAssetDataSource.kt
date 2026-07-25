package com.rudra.defineeasy.feature_dictionary.data.collection

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rudra.defineeasy.core.CrashReporter
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionIds
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
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
        CollectionIds.CONFUSED to "collection_confused.json",
        CollectionIds.GRE to "collection_gre.json",
        CollectionIds.SSC to "collection_ssc.json",
        CollectionIds.IELTS to "collection_ielts.json",
        CollectionIds.TOEFL to "collection_toefl.json",
        CollectionIds.GATE to "collection_gate.json"
    )

    private val cache = ConcurrentHashMap<String, List<CollectionWordDto>>()

    fun getCollectionWords(collectionId: String): List<CollectionWordDto> {
        return cache.getOrPut(collectionId) {
            val fileName = fileNames[collectionId]
                ?: run {
                    CrashReporter.logNonFatal(
                        IllegalArgumentException("Unknown collection id: $collectionId")
                    )
                    return@getOrPut emptyList()
                }
            try {
                context.assets.open(fileName).bufferedReader().use { reader ->
                    if (collectionId == CollectionIds.CONFUSED) {
                        val type = TypeToken.getParameterized(List::class.java, CollectionWordDto::class.java).type
                        gson.fromJson<List<CollectionWordDto>>(reader, type) ?: emptyList()
                    } else {
                        val type = TypeToken.getParameterized(List::class.java, String::class.java).type
                        val strings: List<String> = gson.fromJson(reader, type) ?: emptyList()
                        strings.map { CollectionWordDto(word = it) }
                    }
                }

            } catch (throwable: Throwable) {
                CrashReporter.logNonFatal(throwable)
                emptyList()
            }
        }
    }
}
