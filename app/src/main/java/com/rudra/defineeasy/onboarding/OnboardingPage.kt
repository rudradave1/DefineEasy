package com.rudra.defineeasy.onboarding

import androidx.annotation.StringRes
import com.rudra.defineeasy.R

data class OnboardingPage(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        titleRes = R.string.onboarding_page_one_title,
        descriptionRes = R.string.onboarding_page_one_description
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page_two_title,
        descriptionRes = R.string.onboarding_page_two_description
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page_three_title,
        descriptionRes = R.string.onboarding_page_three_description
    )
)
