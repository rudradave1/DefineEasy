package com.rudra.defineeasy.feature_dictionary.di

import android.app.Application
import androidx.room.Room
import com.google.gson.Gson
import com.rudra.defineeasy.core.CrashReporter
import com.rudra.defineeasy.feature_dictionary.data.local.Converters
import com.rudra.defineeasy.feature_dictionary.data.local.WordInfoDatabase
import com.rudra.defineeasy.feature_dictionary.data.remote.DictionaryApi
import com.rudra.defineeasy.feature_dictionary.data.collection.CollectionRepositoryImpl
import com.rudra.defineeasy.feature_dictionary.data.repository.WordInfoRepositoryImpl
import com.rudra.defineeasy.feature_dictionary.data.util.GsonParser
import com.rudra.defineeasy.feature_dictionary.domain.repository.CollectionRepository
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ClearSearchHistoryUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ClearAllFavoritesUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.DeleteSearchHistoryItemUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetFavoritesUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetDueReviewCountUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetDueReviewWordsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import com.rudra.defineeasy.feature_dictionary.domain.use_case.IsWordFavoritedUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetSavedWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetSearchHistory
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.RateReviewedWordUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ResetReviewProgressUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ToggleFavoriteUseCase
import com.rudra.defineeasy.security.DatabasePassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WordInfoModule {
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideCollectionRepository(
        repositoryImpl: CollectionRepositoryImpl
    ): CollectionRepository {
        return repositoryImpl
    }

    @Provides
    @Singleton
    fun provideGetWordInfoUseCase(repository: WordInfoRepository): GetWordInfo {
        return GetWordInfo(repository)
    }

    @Provides
    @Singleton
    fun provideGetSearchHistoryUseCase(repository: WordInfoRepository): GetSearchHistory {
        return GetSearchHistory(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteSearchHistoryItemUseCase(
        repository: WordInfoRepository
    ): DeleteSearchHistoryItemUseCase {
        return DeleteSearchHistoryItemUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideClearSearchHistoryUseCase(
        repository: WordInfoRepository
    ): ClearSearchHistoryUseCase {
        return ClearSearchHistoryUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideClearAllFavoritesUseCase(
        repository: WordInfoRepository
    ): ClearAllFavoritesUseCase {
        return ClearAllFavoritesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetSavedWordInfoUseCase(repository: WordInfoRepository): GetSavedWordInfo {
        return GetSavedWordInfo(repository)
    }

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(repository: WordInfoRepository): ToggleFavoriteUseCase {
        return ToggleFavoriteUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetFavoritesUseCase(repository: WordInfoRepository): GetFavoritesUseCase {
        return GetFavoritesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetDueReviewWordsUseCase(repository: WordInfoRepository): GetDueReviewWordsUseCase {
        return GetDueReviewWordsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetDueReviewCountUseCase(repository: WordInfoRepository): GetDueReviewCountUseCase {
        return GetDueReviewCountUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRateReviewedWordUseCase(repository: WordInfoRepository): RateReviewedWordUseCase {
        return RateReviewedWordUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideResetReviewProgressUseCase(
        repository: WordInfoRepository
    ): ResetReviewProgressUseCase {
        return ResetReviewProgressUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideIsWordFavoritedUseCase(repository: WordInfoRepository): IsWordFavoritedUseCase {
        return IsWordFavoritedUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideWordInfoRepository(
        db: WordInfoDatabase,
        api: DictionaryApi
    ): WordInfoRepository {
        return WordInfoRepositoryImpl(api, db.dao)
    }

    @Provides
    @Singleton
    fun provideWordInfoDatabase(
        app: Application,
        gson: Gson,
        passphraseProvider: DatabasePassphraseProvider
    ): WordInfoDatabase {
        return try {
            System.loadLibrary("sqlcipher")
            Room.databaseBuilder(
                app, WordInfoDatabase::class.java, "word_db"
            ).addTypeConverter(Converters(GsonParser(gson)))
                .addMigrations(WordInfoDatabase.MIGRATION_1_2)
                .addMigrations(WordInfoDatabase.MIGRATION_2_3)
                .addMigrations(WordInfoDatabase.MIGRATION_3_4)
                .openHelperFactory(
                    SupportOpenHelperFactory(passphraseProvider.getOrCreatePassphrase())
                )
                .build()
        } catch (throwable: Throwable) {
            CrashReporter.logNonFatal(throwable)
            throw throwable
        }
    }

    @Provides
    @Singleton
    fun provideDictionaryApi(): DictionaryApi {
        return Retrofit.Builder()
            .baseUrl(DictionaryApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApi::class.java)
    }
}
