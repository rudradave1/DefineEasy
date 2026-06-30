package com.rudra.defineeasy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rudra.defineeasy.navigation.DefineEasyApp
import com.rudra.defineeasy.ui.theme.DefineEasyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiveShareActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val sharedWord = extractWordFromIntent(intent)
        if (sharedWord.isNullOrBlank()) {
            finish()
            return
        }

        setContent {
            DefineEasyTheme {
                DefineEasyApp(initialSearchWord = sharedWord)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val sharedWord = extractWordFromIntent(intent)
        if (!sharedWord.isNullOrBlank()) {
            setContent {
                DefineEasyTheme {
                    DefineEasyApp(initialSearchWord = sharedWord)
                }
            }
        }
    }

    private fun extractWordFromIntent(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val cleaned = text.trim()
        val word = cleaned.split(Regex("\\s+")).firstOrNull { it.isNotBlank() }
        return word?.filter { it.isLetter() || it == '\'' || it == '-' }?.takeIf { it.length in 2..30 }
    }
}
