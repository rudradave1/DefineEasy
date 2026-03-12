package com.rudra.defineeasy.feature_dictionary.domain.repository

import com.rudra.defineeasy.core.util.Resource
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import kotlinx.coroutines.flow.Flow

interface WordInfoRepository {
    fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>>

    suspend fun getSearchHistory(): List<String>

    suspend fun getSavedWordInfo(word: String): WordInfo?

    suspend fun saveWordInfo(wordInfo: WordInfo)

    suspend fun toggleFavorite(word: String)

    fun getFavorites(): Flow<List<WordInfo>>

    fun isWordFavorited(word: String): Flow<Boolean>

    fun getDueReviewWords(): Flow<List<WordInfo>>

    fun getDueReviewCount(): Flow<Int>

    suspend fun rateReviewedWord(word: String, quality: Int)

    suspend fun deleteSearchHistoryItem(word: String)

    suspend fun clearSearchHistory()

    suspend fun clearAllFavorites()

    suspend fun resetReviewProgress()

}
