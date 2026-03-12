package com.rudra.defineeasy.feature_dictionary.data.repository

import com.rudra.defineeasy.core.CrashReporter
import com.rudra.defineeasy.core.util.Resource
import com.rudra.defineeasy.feature_dictionary.data.local.WordInfoDao
import com.rudra.defineeasy.feature_dictionary.data.local.entity.SearchHistoryEntity
import com.rudra.defineeasy.feature_dictionary.data.local.entity.WordInfoEntity
import com.rudra.defineeasy.feature_dictionary.data.remote.DictionaryApi
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.domain.review.Sm2Scheduler
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import retrofit2.HttpException
import java.io.IOException

class WordInfoRepositoryImpl(
    private val api: DictionaryApi,
    private val dao: WordInfoDao
): WordInfoRepository {

    override fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>> = flow {
        emit(Resource.Loading())
        dao.upsertSearchHistory(
            SearchHistoryEntity(
                word = word,
                searchedAt = System.currentTimeMillis()
            )
        )

        val wordInfos = dao.getWordInfos(word).map { it.toWordInfo() }
        emit(Resource.Loading(data = wordInfos))

        try {
            val remoteWordInfos = api.getWordInfo(word)
            val favoriteMap = remoteWordInfos.associate { remoteWordInfo ->
                remoteWordInfo.word to (dao.getWordInfoExact(remoteWordInfo.word)?.isFavorited ?: false)
            }
            dao.deleteWordInfos(remoteWordInfos.map { it.word })
            dao.insertWordInfos(
                remoteWordInfos.map { remoteWordInfo ->
                    remoteWordInfo.toWordInfoEntity(
                        isFavorited = favoriteMap[remoteWordInfo.word] ?: false
                    )
                }
            )
        } catch(e: HttpException) {
            CrashReporter.logNonFatal(e)
            emit(
                Resource.Error(
                message = "Oops, something went wrong!",
                data = wordInfos
            ))
        } catch(e: IOException) {
            CrashReporter.logNonFatal(e)
            emit(
                Resource.Error(
                message = "Couldn't reach server, check your internet connection.",
                data = wordInfos
            ))
        }

        val newWordInfos = dao.getWordInfos(word).map { it.toWordInfo() }
        emit(Resource.Success(newWordInfos))
    }

    override suspend fun getSearchHistory(): List<String> {
        return dao.getSearchHistory().map { it.word }
    }

    override suspend fun getSavedWordInfo(word: String): WordInfo? {
        return dao.getWordInfoExact(word)?.toWordInfo()
    }

    override suspend fun saveWordInfo(wordInfo: WordInfo) {
        dao.insertWordInfo(
            WordInfoEntity(
                audioUrl = wordInfo.audioUrl,
                easinessFactor = wordInfo.easinessFactor,
                intervalDays = wordInfo.intervalDays,
                isFavorited = wordInfo.isFavorited,
                meanings = wordInfo.meanings,
                nextReviewDateEpochDay = wordInfo.nextReviewDateEpochDay,
                origin = wordInfo.origin,
                phonetic = wordInfo.phonetic,
                repetitions = wordInfo.repetitions,
                word = wordInfo.word
            )
        )
    }

    override suspend fun toggleFavorite(word: String) {
        val existingWordInfo = dao.getWordInfoExact(word) ?: return
        val updatedWordInfo = if (existingWordInfo.isFavorited) {
            existingWordInfo.copy(isFavorited = false)
        } else {
            existingWordInfo.copy(
                isFavorited = true,
                repetitions = 0,
                intervalDays = 0,
                easinessFactor = 2.5,
                nextReviewDateEpochDay = LocalDate.now().toEpochDay()
            )
        }
        dao.insertWordInfo(updatedWordInfo)
    }

    override fun getFavorites(): Flow<List<WordInfo>> {
        return dao.getFavorites().map { words -> words.map { it.toWordInfo() } }
    }

    override fun isWordFavorited(word: String): Flow<Boolean> {
        return dao.isWordFavorited(word)
    }

    override fun getDueReviewWords(): Flow<List<WordInfo>> {
        val today = LocalDate.now().toEpochDay()
        return dao.getDueReviewWords(today).map { words -> words.map(WordInfoEntity::toWordInfo) }
    }

    override fun getDueReviewCount(): Flow<Int> {
        return dao.getDueReviewCount(LocalDate.now().toEpochDay())
    }

    override suspend fun rateReviewedWord(word: String, quality: Int) {
        val existingWordInfo = dao.getWordInfoExact(word) ?: return
        val updatedReview = Sm2Scheduler.calculateNextReview(
            repetitions = existingWordInfo.repetitions,
            intervalDays = existingWordInfo.intervalDays,
            easinessFactor = existingWordInfo.easinessFactor,
            quality = quality,
            todayEpochDay = LocalDate.now().toEpochDay()
        )
        dao.insertWordInfo(
            existingWordInfo.copy(
                repetitions = updatedReview.repetitions,
                intervalDays = updatedReview.intervalDays,
                easinessFactor = updatedReview.easinessFactor,
                nextReviewDateEpochDay = updatedReview.nextReviewDateEpochDay
            )
        )
    }

    override suspend fun deleteSearchHistoryItem(word: String) {
        dao.deleteSearchHistoryItem(word)
    }

    override suspend fun clearSearchHistory() {
        dao.clearSearchHistory()
    }

    override suspend fun clearAllFavorites() {
        dao.clearAllFavoritesAndReviewProgress()
    }

    override suspend fun resetReviewProgress() {
        dao.resetReviewProgress()
    }
}
