package com.rudra.defineeasy.feature_dictionary.domain.review

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SM2AlgorithmTest {

    private val today = 20_000L

    @Test
    fun againRatingIntervalResetsToOneAndRepetitionsReset() {
        val result = Sm2Scheduler.calculateNextReview(
            repetitions = 4,
            intervalDays = 15,
            easinessFactor = 2.5,
            quality = 0,
            todayEpochDay = today
        )

        assertEquals(0, result.repetitions)
        assertEquals(1, result.intervalDays)
    }

    @Test
    fun hardRatingDecreasesEasinessFactorWithMinimum() {
        val result = Sm2Scheduler.calculateNextReview(
            repetitions = 3,
            intervalDays = 10,
            easinessFactor = 2.5,
            quality = 1,
            todayEpochDay = today
        )

        assertEquals(1.96, result.easinessFactor, 0.0001)
        assertEquals(1, result.intervalDays)
    }

    @Test
    fun goodRatingFirstRepSetsIntervalToOne() {
        val result = Sm2Scheduler.calculateNextReview(
            repetitions = 0,
            intervalDays = 0,
            easinessFactor = 2.5,
            quality = 3,
            todayEpochDay = today
        )

        assertEquals(1, result.repetitions)
        assertEquals(1, result.intervalDays)
    }

    @Test
    fun goodRatingSecondRepSetsIntervalToSix() {
        val result = Sm2Scheduler.calculateNextReview(
            repetitions = 1,
            intervalDays = 1,
            easinessFactor = 2.5,
            quality = 3,
            todayEpochDay = today
        )

        assertEquals(2, result.repetitions)
        assertEquals(6, result.intervalDays)
    }

    @Test
    fun goodRatingThirdRepUsesPreviousIntervalTimesEasinessFactor() {
        val result = Sm2Scheduler.calculateNextReview(
            repetitions = 2,
            intervalDays = 6,
            easinessFactor = 2.36,
            quality = 3,
            todayEpochDay = today
        )

        assertEquals((6 * 2.22).toInt(), result.intervalDays)
    }

    @Test
    fun easyRatingIncreasesEasinessFactorAndInterval() {
        val result = Sm2Scheduler.calculateNextReview(
            repetitions = 2,
            intervalDays = 6,
            easinessFactor = 2.5,
            quality = 5,
            todayEpochDay = today
        )

        assertEquals(2.6, result.easinessFactor, 0.0001)
        assertEquals((6 * 2.6).toInt(), result.intervalDays)
    }

    @Test
    fun easinessFactorNeverDropsBelowOnePointThree() {
        val result = Sm2Scheduler.calculateNextReview(
            repetitions = 4,
            intervalDays = 20,
            easinessFactor = 1.3,
            quality = 0,
            todayEpochDay = today
        )

        assertEquals(1.3, result.easinessFactor, 0.0001)
    }

    @Test
    fun nextReviewDateIsAlwaysInTheFuture() {
        val result = Sm2Scheduler.calculateNextReview(
            repetitions = 1,
            intervalDays = 1,
            easinessFactor = 2.5,
            quality = 3,
            todayEpochDay = today
        )

        assertTrue(result.nextReviewDateEpochDay > today)
    }
}
