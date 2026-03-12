package com.rudra.defineeasy.feature_dictionary.data.collection

import com.google.gson.annotations.SerializedName
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord

data class CollectionWordDto(
    val word: String,
    @SerializedName("confused_with") val confusedWith: String? = null
) {
    fun toCollectionWord(): CollectionWord {
        return CollectionWord(
            word = word,
            confusedWith = confusedWith
        )
    }
}
