package com.rudra.defineeasy.feature_dictionary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReviewHistoryEntity(
    val word: String,
    val rating: Int,
    val timestamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
