package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.data.local.WordOfDayAssetDataSource
import com.rudra.defineeasy.feature_dictionary.domain.model.WordOfDay
import javax.inject.Inject

class GetWordOfDayUseCase @Inject constructor(
    private val wordOfDayAssetDataSource: WordOfDayAssetDataSource
) {
    operator fun invoke(): WordOfDay {
        return wordOfDayAssetDataSource.getWordOfDay()
    }
}
