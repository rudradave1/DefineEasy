package com.rudra.defineeasy.feature_dictionary.domain.use_case

import app.cash.turbine.test
import com.rudra.defineeasy.sampleWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetFavoritesUseCaseTest {

    private val repository = mockk<WordInfoRepository>()
    private val useCase = GetFavoritesUseCase(repository)

    @Test
    fun emptyFavoritesEmitsEmptyList() = runTest {
        every { repository.getFavorites() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(emptyList<Any>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun multipleFavoritesEmitsCorrectList() = runTest {
        val favorites = listOf(sampleWordInfo("alpha"), sampleWordInfo("beta"))
        every { repository.getFavorites() } returns flowOf(favorites)

        useCase().test {
            assertEquals(favorites, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun orderIsStable() = runTest {
        val favorites = listOf(sampleWordInfo("alpha"), sampleWordInfo("beta"), sampleWordInfo("gamma"))
        every { repository.getFavorites() } returns flowOf(favorites)

        useCase().test {
            assertEquals(listOf("alpha", "beta", "gamma"), awaitItem().map { it.word })
            awaitComplete()
        }
    }
}
