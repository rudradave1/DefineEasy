package com.rudra.defineeasy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DefineEasyDarkColorScheme = darkColorScheme(
    primary = IndigoSeed,
    onPrimary = TextPrimaryDark,
    primaryContainer = VioletGlow,
    onPrimaryContainer = TextPrimaryDark,
    secondary = ReviewAmber,
    onSecondary = DeepIndigo,
    secondaryContainer = CardSurface,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = EasyGreen,
    onTertiary = DeepIndigo,
    tertiaryContainer = ElevatedSurface,
    onTertiaryContainer = TextPrimaryDark,
    background = MidnightSurface,
    onBackground = TextPrimaryDark,
    surface = MidnightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = ElevatedSurface,
    surfaceContainerLow = ElevatedSurface,
    surfaceContainerHigh = CardSurface,
    surfaceContainerHighest = CardSurface,
    outline = SoftOutline,
    error = HardRed,
    onError = TextPrimaryDark,
    errorContainer = HardRed.copy(alpha = 0.2f),
    onErrorContainer = HardRed
)

private val DefineEasyLightColorScheme = lightColorScheme(
    primary = IndigoSeed,
    onPrimary = TextPrimaryDark,
    primaryContainer = IndigoSeed.copy(alpha = 0.18f),
    onPrimaryContainer = DeepIndigo,
    secondary = GoodAmber,
    onSecondary = DeepIndigo,
    tertiary = EasyGreen,
    onTertiary = DeepIndigo
)

@Composable
fun DefineEasyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context).copy(
                    primary = IndigoSeed,
                    primaryContainer = VioletGlow,
                    secondary = ReviewAmber,
                    tertiary = EasyGreen,
                    background = MidnightSurface,
                    surface = MidnightSurface,
                    surfaceVariant = CardSurface,
                    surfaceContainer = ElevatedSurface,
                    surfaceContainerLow = ElevatedSurface,
                    surfaceContainerHigh = CardSurface,
                    surfaceContainerHighest = CardSurface,
                    onBackground = TextPrimaryDark,
                    onSurface = TextPrimaryDark,
                    onSurfaceVariant = TextSecondaryDark,
                    outline = SoftOutline,
                    error = HardRed,
                    onError = TextPrimaryDark
                )
            } else {
                dynamicLightColorScheme(context).copy(
                    primary = IndigoSeed,
                    secondary = GoodAmber,
                    tertiary = EasyGreen
                )
            }
        }

        darkTheme -> DefineEasyDarkColorScheme
        else -> DefineEasyLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
