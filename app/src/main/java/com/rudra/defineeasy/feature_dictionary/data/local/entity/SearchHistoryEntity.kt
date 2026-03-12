package com.rudra.defineeasy.feature_dictionary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SearchHistoryEntity(
    @PrimaryKey val word: String,
    val searchedAt: Long
)
