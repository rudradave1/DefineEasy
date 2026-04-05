package com.rudra.defineeasy.onboarding

import androidx.annotation.StringRes
import com.rudra.defineeasy.R

data class OnboardingPage(
    @param:StringRes val emojiRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        emojiRes = R.string.onboarding_page_one_emoji,
        titleRes = R.string.onboarding_page_one_new_title,
        descriptionRes = R.string.onboarding_page_one_new_description
    ),
    OnboardingPage(
        emojiRes = R.string.onboarding_page_two_emoji,
        titleRes = R.string.onboarding_page_two_new_title,
        descriptionRes = R.string.onboarding_page_two_new_description
    ),
    OnboardingPage(
        emojiRes = R.string.onboarding_page_three_emoji,
        titleRes = R.string.onboarding_page_three_new_title,
        descriptionRes = R.string.onboarding_page_three_new_description
    )
)
