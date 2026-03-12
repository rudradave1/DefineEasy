package com.rudra.defineeasy.feature_dictionary.domain.model


data class WordInfo(
    val audioUrl: String,
    val easinessFactor: Double,
    val intervalDays: Int,
    val isFavorited: Boolean,
    val meanings: List<Meaning>,
    val nextReviewDateEpochDay: Long,
    val origin: String,
    val phonetic: String,
    val repetitions: Int,
    val word: String
)
