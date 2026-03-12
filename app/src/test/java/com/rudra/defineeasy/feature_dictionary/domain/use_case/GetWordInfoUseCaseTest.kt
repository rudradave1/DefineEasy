package com.rudra.defineeasy.feature_dictionary.domain.use_case

import app.cash.turbine.test
import com.rudra.defineeasy.sampleWordInfo
import com.rudra.defineeasy.core.util.Resource
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetWordInfoUseCaseTest {

    private val repository = mockk<WordInfoRepository>()
    private val useCase = GetWordInfo(repository)

    @Test
    fun returnsCachedDataImmediately() = runTest {
        val cached = listOf(sampleWordInfo("cached"))
        every { repository.getWordInfo("word") } returns flowOf(
            Resource.Loading(cached),
            Resource.Success(cached)
        )

        useCase("word").test {
            assertEquals(cached, (awaitItem() as Resource.Loading).data)
            assertEquals(cached, (awaitItem() as Resource.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun fetchesFreshDataWhenCacheStale() = runTest {
        val cached = listOf(sampleWordInfo("cached"))
        val fresh = listOf(sampleWordInfo("fresh"))
        every { repository.getWordInfo("word") } returns flowOf(
            Resource.Loading(cached),
            Resource.Success(fresh)
        )

        useCase("word").test {
            assertEquals(cached, (awaitItem() as Resource.Loading).data)
            assertEquals(fresh, (awaitItem() as Resource.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun returnsErrorStateWhenApiFailsAndNoCacheExists() = runTest {
        every { repository.getWordInfo("word") } returns flowOf(
            Resource.Loading(),
            Resource.Error(message = "network", data = emptyList())
        )

        useCase("word").test {
            awaitItem()
            assertEquals("network", (awaitItem() as Resource.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun returnsCachedDataWhenApiFailsButCacheExists() = runTest {
        val cached = listOf(sampleWordInfo("cached"))
        every { repository.getWordInfo("word") } returns flowOf(
            Resource.Loading(cached),
            Resource.Error(message = "network", data = cached)
        )

        useCase("word").test {
            assertEquals(cached, (awaitItem() as Resource.Loading).data)
            assertEquals(cached, (awaitItem() as Resource.Error).data)
            awaitComplete()
        }
    }
}
