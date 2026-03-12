package com.rudra.defineeasy.feature_dictionary.domain.model

object CollectionIds {
    const val UPSC = "upsc"
    const val CAT = "cat"
    const val BUSINESS = "business"
    const val CONFUSED = "confused"
}

data class CollectionSummary(
    val id: String,
    val wordCount: Int
)

data class CollectionWord(
    val word: String,
    val confusedWith: String? = null
)
