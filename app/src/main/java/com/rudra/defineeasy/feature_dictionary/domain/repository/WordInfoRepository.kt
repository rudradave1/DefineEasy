package com.rudra.defineeasy.feature_dictionary.domain.repository

import com.rudra.defineeasy.core.util.Resource
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import kotlinx.coroutines.flow.Flow

interface WordInfoRepository {
    fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>>

    suspend fun getSearchHistory(): List<String>

}