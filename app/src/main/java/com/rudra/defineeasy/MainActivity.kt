package com.rudra.defineeasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rudra.defineeasy.navigation.DefineEasyApp
import com.rudra.defineeasy.ui.theme.DefineEasyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_OPEN_TAB = "extra_open_tab"
        const val TAB_REVIEW = "review"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            DefineEasyTheme {
                DefineEasyApp()
            }
        }
    }
}
