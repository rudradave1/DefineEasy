package com.rudra.defineeasy.settings

import com.rudra.defineeasy.core.analytics.AnalyticsService
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ClearAllFavoritesUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ClearSearchHistoryUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ResetReviewProgressUseCase
import com.rudra.defineeasy.notifications.ReviewReminderScheduler
import com.rudra.defineeasy.preferences.ReminderSettings
import com.rudra.defineeasy.preferences.ReminderPreferences
import com.rudra.defineeasy.preferences.ThemeMode
import com.rudra.defineeasy.preferences.ThemePreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val reminderPreferences = mockk<ReminderPreferences>()
    private val reviewReminderScheduler = mockk<ReviewReminderScheduler>()
    private val clearSearchHistoryUseCase = mockk<ClearSearchHistoryUseCase>()
    private val clearAllFavoritesUseCase = mockk<ClearAllFavoritesUseCase>()
    private val resetReviewProgressUseCase = mockk<ResetReviewProgressUseCase>()
    private val analyticsService = mockk<AnalyticsService>(relaxed = true)
    private val themePreferences = mockk<ThemePreferences>()

    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { reminderPreferences.reminderSettings() } returns flowOf(ReminderSettings())
        every { themePreferences.themeMode } returns themeModeFlow
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsDefaultStateOnInit() = runTest {
        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.reminderEnabled)
        assertEquals(20, viewModel.uiState.value.reminderHour)
        assertEquals(0, viewModel.uiState.value.reminderMinute)
    }

    @Test
    fun loadsSavedReminderSettings() = runTest {
        every { reminderPreferences.reminderSettings() } returns flowOf(
            ReminderSettings(enabled = false, hour = 8, minute = 30)
        )

        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.reminderEnabled)
        assertEquals(8, viewModel.uiState.value.reminderHour)
        assertEquals(30, viewModel.uiState.value.reminderMinute)
    }

    @Test
    fun loadsThemeMode() = runTest {
        themeModeFlow.value = ThemeMode.DARK

        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun setThemeModeUpdatesPreferences() = runTest {
        coEvery { themePreferences.setThemeMode(ThemeMode.DARK) } just runs

        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        coVerify { themePreferences.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun enableReminderSchedulesAndLogsAnalytics() = runTest {
        every { reviewReminderScheduler.reschedule(any(), any()) } just runs
        coEvery { reminderPreferences.setReminderEnabled(true) } just runs

        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        viewModel.setReminderEnabled(true)
        advanceUntilIdle()

        coVerify { reminderPreferences.setReminderEnabled(true) }
        verify { analyticsService.onReminderEnabled(true) }
        verify { reviewReminderScheduler.reschedule(20, 0) }
    }

    @Test
    fun disableReminderCancelsAndLogsAnalytics() = runTest {
        every { reviewReminderScheduler.cancel() } just runs
        coEvery { reminderPreferences.setReminderEnabled(false) } just runs

        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        viewModel.setReminderEnabled(false)
        advanceUntilIdle()

        coVerify { reminderPreferences.setReminderEnabled(false) }
        verify { analyticsService.onReminderEnabled(false) }
        verify { reviewReminderScheduler.cancel() }
    }

    @Test
    fun setReminderTimeUpdatesAndReschedulesWhenEnabled() = runTest {
        every { reviewReminderScheduler.reschedule(any(), any()) } just runs
        coEvery { reminderPreferences.setReminderTime(7, 30) } just runs

        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        viewModel.setReminderTime(7, 30)
        advanceUntilIdle()

        coVerify { reminderPreferences.setReminderTime(7, 30) }
        verify { reviewReminderScheduler.reschedule(7, 30) }
    }

    @Test
    fun clearSearchHistoryDelegates() = runTest {
        coEvery { clearSearchHistoryUseCase() } just runs

        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        viewModel.clearSearchHistory()
        advanceUntilIdle()

        coVerify { clearSearchHistoryUseCase() }
    }

    @Test
    fun clearAllFavoritesDelegates() = runTest {
        coEvery { clearAllFavoritesUseCase() } just runs

        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        viewModel.clearAllFavorites()
        advanceUntilIdle()

        coVerify { clearAllFavoritesUseCase() }
    }

    @Test
    fun resetReviewProgressDelegates() = runTest {
        coEvery { resetReviewProgressUseCase() } just runs

        val viewModel = SettingsViewModel(
            reminderPreferences, reviewReminderScheduler,
            clearSearchHistoryUseCase, clearAllFavoritesUseCase,
            resetReviewProgressUseCase, analyticsService, themePreferences
        )
        advanceUntilIdle()

        viewModel.resetReviewProgress()
        advanceUntilIdle()

        coVerify { resetReviewProgressUseCase() }
    }
}
