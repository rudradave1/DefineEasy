package com.rudra.defineeasy.feature_dictionary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CustomCollectionEntity(
    val name: String,
    val createdAt: Long,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(primaryKeys = ["collectionId", "word"])
data class CollectionWordCrossRef(
    val collectionId: Int,
    val word: String
)
