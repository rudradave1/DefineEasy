package com.rudra.defineeasy.feature_dictionary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.defineeasy.feature_dictionary.data.local.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewHistory(history: ReviewHistoryEntity)

    @Query("SELECT * FROM ReviewHistoryEntity ORDER BY timestamp DESC")
    fun getReviewHistory(): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT COUNT(*) FROM ReviewHistoryEntity WHERE timestamp >= :startTime")
    fun getReviewCountSince(startTime: Long): Flow<Int>
}
