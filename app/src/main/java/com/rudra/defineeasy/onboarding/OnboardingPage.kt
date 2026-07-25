package com.rudra.defineeasy.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rudra.defineeasy.R

data class OnboardingPage(
    @DrawableRes val drawableRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        drawableRes = R.drawable.ic_onboarding_learn,
        titleRes = R.string.onboarding_page_one_new_title,
        descriptionRes = R.string.onboarding_page_one_new_description
    ),
    OnboardingPage(
        drawableRes = R.drawable.ic_onboarding_streak,
        titleRes = R.string.onboarding_page_two_new_title,
        descriptionRes = R.string.onboarding_page_two_new_description
    ),
    OnboardingPage(
        drawableRes = R.drawable.ic_onboarding_progress,
        titleRes = R.string.onboarding_page_three_new_title,
        descriptionRes = R.string.onboarding_page_three_new_description
    )
)
