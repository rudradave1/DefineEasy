package com.rudra.defineeasy.settings

import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.defineeasy.BuildConfig
import com.rudra.defineeasy.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class SettingsDialog {
    CLEAR_HISTORY,
    CLEAR_FAVORITES,
    RESET_REVIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var activeDialog by remember { mutableStateOf<SettingsDialog?>(null) }
    BackHandler(onBack = onNavigateUp)

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSectionTitle(stringResource(R.string.settings_notifications_section))
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_daily_reminder),
                    checked = uiState.reminderEnabled,
                    onCheckedChange = viewModel::setReminderEnabled
                )
                SettingsClickableRow(
                    title = stringResource(R.string.settings_reminder_time),
                    value = formatTime(uiState.reminderHour, uiState.reminderMinute),
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.setReminderTime(hour, minute)
                            },
                            uiState.reminderHour,
                            uiState.reminderMinute,
                            false
                        ).show()
                    }
                )
            }

            item {
                SettingsSectionTitle(stringResource(R.string.settings_data_section))
                SettingsActionRow(
                    title = stringResource(R.string.settings_clear_search_history),
                    onClick = { activeDialog = SettingsDialog.CLEAR_HISTORY }
                )
                SettingsActionRow(
                    title = stringResource(R.string.settings_clear_all_favorites),
                    onClick = { activeDialog = SettingsDialog.CLEAR_FAVORITES }
                )
                SettingsActionRow(
                    title = stringResource(R.string.settings_reset_review_progress),
                    onClick = { activeDialog = SettingsDialog.RESET_REVIEW }
                )
            }

            item {
                SettingsSectionTitle(stringResource(R.string.settings_about_section))
                SettingsClickableRow(
                    title = stringResource(R.string.settings_version),
                    value = BuildConfig.VERSION_NAME,
                    onClick = {}
                )
                SettingsActionRow(
                    title = stringResource(R.string.settings_rate_on_play_store),
                    onClick = {
                        val marketIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=${context.packageName}")
                        )
                        val webIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                        )
                        try {
                            context.startActivity(marketIntent)
                        } catch (_: ActivityNotFoundException) {
                            context.startActivity(webIntent)
                        }
                    }
                )
                SettingsActionRow(
                    title = stringResource(R.string.settings_privacy_policy),
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://defineeasy.app/privacy"))
                        )
                    }
                )
            }
        }
    }

    when (activeDialog) {
        SettingsDialog.CLEAR_HISTORY -> ConfirmationDialog(
            title = stringResource(R.string.settings_clear_search_history),
            message = stringResource(R.string.settings_clear_search_history_message),
            onConfirm = {
                viewModel.clearSearchHistory()
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )
        SettingsDialog.CLEAR_FAVORITES -> ConfirmationDialog(
            title = stringResource(R.string.settings_clear_all_favorites),
            message = stringResource(R.string.settings_clear_all_favorites_message),
            onConfirm = {
                viewModel.clearAllFavorites()
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )
        SettingsDialog.RESET_REVIEW -> ConfirmationDialog(
            title = stringResource(R.string.settings_reset_review_progress),
            message = stringResource(R.string.settings_reset_review_progress_message),
            onConfirm = {
                viewModel.resetReviewProgress()
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )
        null -> Unit
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SettingsClickableRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SettingsActionRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    return LocalTime.of(hour, minute).format(formatter)
}
