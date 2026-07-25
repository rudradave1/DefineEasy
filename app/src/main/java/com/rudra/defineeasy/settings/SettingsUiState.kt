package com.rudra.defineeasy.settings

import com.rudra.defineeasy.preferences.ThemeMode

data class SettingsUiState(
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true
)
