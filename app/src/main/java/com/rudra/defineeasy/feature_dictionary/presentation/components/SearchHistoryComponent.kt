package com.rudra.defineeasy.feature_dictionary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rudra.defineeasy.R

@Composable
fun SearchHistoryComponent(
    words: List<String>,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = modifier) {
        if (words.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.search_history))
                TextButton(onClick = onClearAll) {
                    Text(text = stringResource(R.string.clear_all))
                }
            }
            LazyColumn {
                items(words, key = { it }) { word ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .pointerInput(word) {
                                var totalDrag = 0f
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, dragAmount ->
                                        totalDrag += dragAmount
                                    },
                                    onDragEnd = {
                                        if (totalDrag < -120f) {
                                            onDelete(word)
                                        }
                                        totalDrag = 0f
                                    }
                                )
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onClick(word) }
                        ) {
                            Text(text = word)
                        }
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete_recent_search),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        } else {
            Text(text = stringResource(R.string.no_search_history))
        }
    }
}
