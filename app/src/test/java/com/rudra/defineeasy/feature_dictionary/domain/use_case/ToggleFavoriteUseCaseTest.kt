package com.rudra.defineeasy.feature_dictionary.domain.use_case

import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ToggleFavoriteUseCaseTest {

    private val repository = mockk<WordInfoRepository> {
        coEvery { toggleFavorite("word") } just runs
    }
    private val useCase = ToggleFavoriteUseCase(repository)

    @Test
    fun unfavoritedWordBecomesFavorited() = runTest {
        useCase("word")
        coVerify { repository.toggleFavorite("word") }
    }

    @Test
    fun favoritedWordBecomesUnfavorited() = runTest {
        useCase("word")
        coVerify { repository.toggleFavorite("word") }
    }
}
