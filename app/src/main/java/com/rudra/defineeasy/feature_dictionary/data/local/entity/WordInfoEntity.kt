package com.rudra.defineeasy.feature_dictionary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.defineeasy.feature_dictionary.domain.model.Meaning
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo

@Entity
data class WordInfoEntity(
    val word: String,
    val phonetic: String,
    val origin: String,
    val audioUrl: String,
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val easinessFactor: Double = 2.5,
    val nextReviewDateEpochDay: Long = 0,
    val isFavorited: Boolean = false,
    val meanings: List<Meaning>,
    @PrimaryKey val id: Int? = null
) {
    fun toWordInfo(): WordInfo {
        return WordInfo(
            audioUrl = audioUrl,
            easinessFactor = easinessFactor,
            intervalDays = intervalDays,
            isFavorited = isFavorited,
            meanings = meanings,
            nextReviewDateEpochDay = nextReviewDateEpochDay,
            word = word,
            origin = origin,
            phonetic = phonetic,
            repetitions = repetitions
        )
    }
}
