package com.rudra.defineeasy.settings

data class SettingsUiState(
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val isLoading: Boolean = true
)
