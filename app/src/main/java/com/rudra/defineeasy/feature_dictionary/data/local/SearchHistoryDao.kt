package com.rudra.defineeasy.feature_dictionary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.defineeasy.feature_dictionary.data.local.entity.SearchHistoryEntity

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearchHistory(searchHistoryEntity: SearchHistoryEntity)

    @Query("SELECT * FROM SearchHistoryEntity ORDER BY searchedAt DESC LIMIT 10")
    suspend fun getSearchHistory(): List<SearchHistoryEntity>

    @Query("DELETE FROM SearchHistoryEntity WHERE LOWER(word) = LOWER(:word)")
    suspend fun deleteSearchHistoryItem(word: String)

    @Query("DELETE FROM SearchHistoryEntity")
    suspend fun clearSearchHistory()
}
