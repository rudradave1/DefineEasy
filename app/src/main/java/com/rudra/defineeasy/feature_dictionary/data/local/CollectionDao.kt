package com.rudra.defineeasy.feature_dictionary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.rudra.defineeasy.feature_dictionary.data.local.entity.CollectionWordCrossRef
import com.rudra.defineeasy.feature_dictionary.data.local.entity.CustomCollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCollection(collection: CustomCollectionEntity): Long

    @Query("SELECT * FROM CustomCollectionEntity ORDER BY createdAt DESC")
    fun getCustomCollections(): Flow<List<CustomCollectionEntity>>

    @Query("DELETE FROM CustomCollectionEntity WHERE id = :collectionId")
    suspend fun deleteCustomCollection(collectionId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionWordCrossRef(crossRef: CollectionWordCrossRef)

    @Query("DELETE FROM CollectionWordCrossRef WHERE collectionId = :collectionId AND word = :word")
    suspend fun deleteCollectionWordCrossRef(collectionId: Int, word: String)

    @Transaction
    @Query("SELECT word FROM CollectionWordCrossRef WHERE collectionId = :collectionId")
    fun getWordsForCollection(collectionId: Int): Flow<List<String>>
}
