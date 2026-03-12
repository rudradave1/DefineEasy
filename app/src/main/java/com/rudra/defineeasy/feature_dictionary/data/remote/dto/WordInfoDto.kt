package com.rudra.defineeasy.feature_dictionary.data.remote.dto

import com.rudra.defineeasy.feature_dictionary.data.local.entity.WordInfoEntity

data class WordInfoDto(
    val meanings: List<MeaningDto>,
    val origin: String?,
    val phonetic: String?,
    val phonetics: List<PhoneticDto>,
    val word: String
) {
    fun toWordInfoEntity(isFavorited: Boolean = false): WordInfoEntity {
        return WordInfoEntity(
            audioUrl = phonetics.firstOrNull { it.audio.isNotBlank() }?.audio ?: "",
            isFavorited = isFavorited,
            meanings = meanings.map { it.toMeaning() },
            nextReviewDateEpochDay = 0,
            origin = origin ?: "",
            phonetic = phonetic ?: "",
            easinessFactor = 2.5,
            intervalDays = 0,
            repetitions = 0,
            word = word
        )
    }
}
