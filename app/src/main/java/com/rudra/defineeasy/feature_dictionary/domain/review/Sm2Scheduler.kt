package com.rudra.defineeasy.feature_dictionary.domain.review

data class ReviewSchedule(
    val repetitions: Int,
    val intervalDays: Int,
    val easinessFactor: Double,
    val nextReviewDateEpochDay: Long
)

object Sm2Scheduler {
    fun calculateNextReview(
        repetitions: Int,
        intervalDays: Int,
        easinessFactor: Double,
        quality: Int,
        todayEpochDay: Long
    ): ReviewSchedule {
        val updatedEasinessFactor = (
            easinessFactor + (
                0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)
                )
            ).coerceAtLeast(1.3)

        val (updatedRepetitions, updatedIntervalDays) = if (quality < 3) {
            0 to 1
        } else {
            when (repetitions) {
                0 -> 1 to 1
                1 -> 2 to 6
                else -> (repetitions + 1) to (intervalDays * updatedEasinessFactor).toInt().coerceAtLeast(1)
            }
        }

        return ReviewSchedule(
            repetitions = updatedRepetitions,
            intervalDays = updatedIntervalDays,
            easinessFactor = updatedEasinessFactor,
            nextReviewDateEpochDay = todayEpochDay + updatedIntervalDays
        )
    }
}
