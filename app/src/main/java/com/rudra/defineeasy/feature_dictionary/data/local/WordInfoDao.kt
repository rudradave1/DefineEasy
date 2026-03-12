package com.rudra.defineeasy.feature_dictionary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.defineeasy.feature_dictionary.data.local.entity.SearchHistoryEntity
import com.rudra.defineeasy.feature_dictionary.data.local.entity.WordInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordInfos(infos: List<WordInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordInfo(info: WordInfoEntity)

    @Query("DELETE FROM wordinfoentity WHERE word IN(:words)")
    suspend fun deleteWordInfos(words: List<String>)

    @Query("SELECT * FROM wordinfoentity WHERE word LIKE '%' || :word || '%'")
    suspend fun getWordInfos(word: String): List<WordInfoEntity>

    @Query("SELECT * FROM wordinfoentity WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun getWordInfoExact(word: String): WordInfoEntity?

    @Query("UPDATE wordinfoentity SET isFavorited = NOT isFavorited WHERE LOWER(word) = LOWER(:word)")
    suspend fun toggleFavorite(word: String)

    @Query("SELECT * FROM wordinfoentity WHERE isFavorited = 1 ORDER BY word COLLATE NOCASE ASC")
    fun getFavorites(): Flow<List<WordInfoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM wordinfoentity WHERE LOWER(word) = LOWER(:word) AND isFavorited = 1)")
    fun isWordFavorited(word: String): Flow<Boolean>

    @Query(
        "SELECT * FROM wordinfoentity WHERE isFavorited = 1 AND nextReviewDateEpochDay <= :todayEpochDay ORDER BY nextReviewDateEpochDay ASC, word COLLATE NOCASE ASC"
    )
    fun getDueReviewWords(todayEpochDay: Long): Flow<List<WordInfoEntity>>

    @Query(
        "SELECT COUNT(*) FROM wordinfoentity WHERE isFavorited = 1 AND nextReviewDateEpochDay <= :todayEpochDay"
    )
    fun getDueReviewCount(todayEpochDay: Long): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM wordinfoentity WHERE isFavorited = 1 AND nextReviewDateEpochDay <= :todayEpochDay"
    )
    suspend fun getDueReviewCountValue(todayEpochDay: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearchHistory(searchHistoryEntity: SearchHistoryEntity)

    @Query("SELECT * FROM SearchHistoryEntity ORDER BY searchedAt DESC LIMIT 10")
    suspend fun getSearchHistory(): List<SearchHistoryEntity>

    @Query("DELETE FROM SearchHistoryEntity WHERE LOWER(word) = LOWER(:word)")
    suspend fun deleteSearchHistoryItem(word: String)

    @Query("DELETE FROM SearchHistoryEntity")
    suspend fun clearSearchHistory()

    @Query(
        """
        UPDATE wordinfoentity
        SET isFavorited = 0,
            repetitions = 0,
            intervalDays = 0,
            easinessFactor = 2.5,
            nextReviewDateEpochDay = 0
        """
    )
    suspend fun clearAllFavoritesAndReviewProgress()

    @Query(
        """
        UPDATE wordinfoentity
        SET repetitions = 0,
            intervalDays = 0,
            easinessFactor = 2.5,
            nextReviewDateEpochDay = 0
        """
    )
    suspend fun resetReviewProgress()
}
