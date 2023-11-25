package com.rudra.defineeasy.feature_dictionary.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchHistoryComponent(
    words: Set<String>,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
) {
    Column(modifier = modifier) {
        if (words.isNotEmpty()) {
            words.forEach { word ->
                Text(
                    text = word,
                    modifier = Modifier.clickable {
                        onClick(word)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text(text = "There is no history yet...")
        }
    }
}