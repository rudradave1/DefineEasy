package com.rudra.defineeasy.feature_dictionary.domain.model

object CollectionIds {
    const val UPSC = "upsc"
    const val CAT = "cat"
    const val BUSINESS = "business"
    const val CONFUSED = "confused"
    const val GRE = "gre"
    const val SSC = "ssc"
    const val IELTS = "ielts"
    const val TOEFL = "toefl"
    const val GATE = "gate"
}

data class CollectionSummary(
    val id: String,
    val wordCount: Int
)

data class CollectionWord(
    val word: String,
    val confusedWith: String? = null
)
