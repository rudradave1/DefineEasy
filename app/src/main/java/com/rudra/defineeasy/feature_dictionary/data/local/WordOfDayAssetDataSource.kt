package com.rudra.defineeasy.feature_dictionary.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rudra.defineeasy.core.CrashReporter
import com.rudra.defineeasy.feature_dictionary.domain.model.WordOfDay
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate

@Singleton
class WordOfDayAssetDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private var cachedWords: List<WordOfDay>? = null

    fun getWordOfDay(): WordOfDay {
        val words = cachedWords ?: loadWords().also { cachedWords = it }
        val index = (LocalDate.now().dayOfYear - 1) % words.size
        return words[index]
    }

    private fun loadWords(): List<WordOfDay> {
        return try {
            context.assets.open("wotd.json").bufferedReader().use { reader ->
                val type = TypeToken.getParameterized(List::class.java, WordOfDay::class.java).type
                gson.fromJson(reader, type)
            }
        } catch (throwable: Throwable) {
            CrashReporter.logNonFatal(throwable)
            throw throwable
        }
    }
}
