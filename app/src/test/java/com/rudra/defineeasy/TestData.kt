package com.rudra.defineeasy

import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo

fun sampleWordInfo(word: String) = WordInfo(
    audioUrl = "",
    easinessFactor = 2.5,
    intervalDays = 0,
    isFavorited = false,
    meanings = emptyList(),
    nextReviewDateEpochDay = 0,
    origin = "",
    phonetic = "",
    repetitions = 0,
    word = word
)
