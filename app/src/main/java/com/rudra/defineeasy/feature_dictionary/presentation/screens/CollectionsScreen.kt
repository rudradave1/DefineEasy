package com.rudra.defineeasy.feature_dictionary.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.defineeasy.R
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionIds
import com.rudra.defineeasy.feature_dictionary.presentation.CollectionCardUiModel
import com.rudra.defineeasy.feature_dictionary.presentation.CollectionsUiState
import com.rudra.defineeasy.feature_dictionary.presentation.CollectionsViewModel
import com.rudra.defineeasy.feature_dictionary.presentation.collectionUiMetadata
import com.rudra.defineeasy.ui.theme.BusinessGradientEnd
import com.rudra.defineeasy.ui.theme.BusinessGradientStart
import com.rudra.defineeasy.ui.theme.CatGradientEnd
import com.rudra.defineeasy.ui.theme.CatGradientStart
import com.rudra.defineeasy.ui.theme.DefineEasyTheme
import com.rudra.defineeasy.ui.theme.GeneralGradientEnd
import com.rudra.defineeasy.ui.theme.GeneralGradientStart
import com.rudra.defineeasy.ui.theme.GreGradientEnd
import com.rudra.defineeasy.ui.theme.GreGradientStart
import com.rudra.defineeasy.ui.theme.UpscGradientEnd
import com.rudra.defineeasy.ui.theme.UpscGradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onCollectionSelected: (String) -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.collections_tab),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            CollectionsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            CollectionsUiState.Empty -> {
                EmptyCollectionsState(modifier = Modifier.padding(paddingValues))
            }

            is CollectionsUiState.Error -> {
                ErrorCollectionsState(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is CollectionsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.collections, key = { it.id }) { collection ->
                        CollectionCard(
                            collection = collection,
                            onClick = { onCollectionSelected(collection.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionCard(
    collection: CollectionCardUiModel,
    onClick: () -> Unit
) {
    val metadata = collectionUiMetadata(collection.id)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "collection_card_scale"
    )
    val gradient = when (collection.id) {
        CollectionIds.UPSC -> listOf(UpscGradientStart, UpscGradientEnd)
        CollectionIds.CAT -> listOf(CatGradientStart, CatGradientEnd)
        CollectionIds.BUSINESS -> listOf(BusinessGradientStart, BusinessGradientEnd)
        CollectionIds.CONFUSED -> listOf(GreGradientStart, GreGradientEnd)
        else -> listOf(GeneralGradientStart, GeneralGradientEnd)
    }
    val reviewedCount = (collection.wordCount * collection.completionPercentage) / 100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradient))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(metadata.titleRes),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.collection_word_count, collection.wordCount),
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = metadata.icon,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.collections_progress_label, collection.completionPercentage),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    LinearProgressIndicator(
                        progress = { collection.completionPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.22f)
                    )
                    Text(
                        text = "$reviewedCount / ${collection.wordCount}",
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCollectionsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = stringResource(R.string.no_collections_available))
        }
    }
}

@Composable
private fun ErrorCollectionsState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.collections_error_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = message)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollectionCardPreview() {
    DefineEasyTheme {
        CollectionCard(
            collection = CollectionCardUiModel(
                id = CollectionIds.UPSC,
                wordCount = 200,
                completionPercentage = 42
            ),
            onClick = {}
        )
    }
}
