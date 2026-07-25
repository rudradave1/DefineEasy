package com.rudra.defineeasy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rudra.defineeasy.navigation.DefineEasyApp
import com.rudra.defineeasy.preferences.ThemePreferences
import com.rudra.defineeasy.ui.theme.DefineEasyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themePreferences: ThemePreferences

    companion object {
        const val EXTRA_OPEN_TAB = "extra_open_tab"
        const val TAB_REVIEW = "review"
        const val TAB_WOTD = "wotd"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = com.rudra.defineeasy.preferences.ThemeMode.SYSTEM)
            DefineEasyTheme(themeMode = themeMode) {
                DefineEasyApp()
            }
        }
    }
}
