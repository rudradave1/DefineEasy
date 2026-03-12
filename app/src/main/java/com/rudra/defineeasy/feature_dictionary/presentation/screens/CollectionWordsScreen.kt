package com.rudra.defineeasy.feature_dictionary.presentation.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.defineeasy.R
import com.rudra.defineeasy.feature_dictionary.presentation.CollectionWordUiModel
import com.rudra.defineeasy.feature_dictionary.presentation.CollectionWordsViewModel
import com.rudra.defineeasy.feature_dictionary.presentation.WordDefinitionState
import com.rudra.defineeasy.feature_dictionary.presentation.collectionUiMetadata
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CollectionWordsScreenRoute(
    onBackClick: () -> Unit,
    onWordSelected: (String) -> Unit,
    viewModel: CollectionWordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is CollectionWordsViewModel.CollectionWordsEvent.OpenWordDetail -> {
                    onWordSelected(event.word)
                }
            }
        }
    }

    CollectionWordsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onWordClick = viewModel::openWord,
        onAddToReviewClick = { item ->
            viewModel.addToReview(item.word, item.isInReview)
        },
        onLoadDefinition = viewModel::loadDefinitionForWord,
        onCheckCache = viewModel::checkCacheForWord
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionWordsScreen(
    uiState: com.rudra.defineeasy.feature_dictionary.presentation.CollectionWordsUiState,
    onBackClick: () -> Unit,
    onWordClick: (com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord) -> Unit,
    onAddToReviewClick: (CollectionWordUiModel) -> Unit,
    onLoadDefinition: (String) -> Unit,
    onCheckCache: (String) -> Unit
) {
    val metadata = collectionUiMetadata(uiState.collectionId)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(metadata.titleRes)) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.collection_words_error_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = uiState.errorMessage)
                    }
                }
            }

            uiState.words.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.no_collection_words))
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(uiState.words, key = { it.word.word }) { item ->
                        CollectionWordRow(
                            item = item,
                            definitionState = uiState.definitionStates[item.word.word],
                            onWordClick = { onWordClick(item.word) },
                            onAddToReviewClick = { onAddToReviewClick(item) },
                            onLoadDefinition = { onLoadDefinition(item.word.word) },
                            onCheckCache = { onCheckCache(item.word.word) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionWordRow(
    item: CollectionWordUiModel,
    definitionState: WordDefinitionState?,
    onWordClick: () -> Unit,
    onAddToReviewClick: () -> Unit,
    onLoadDefinition: () -> Unit,
    onCheckCache: () -> Unit
) {
    LaunchedEffect(item.word.word) {
        onCheckCache()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (definitionState) {
                    null, is WordDefinitionState.Failed -> onLoadDefinition()
                    is WordDefinitionState.Loaded -> onWordClick()
                    is WordDefinitionState.Loading -> { /* Ignore tap while loading */ }
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.word.word,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                item.word.confusedWith?.takeIf { it.isNotBlank() }?.let { confusedWith ->
                    Text(
                        text = stringResource(R.string.confused_with_label, confusedWith),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            AssistChip(
                onClick = onAddToReviewClick,
                label = {
                    Text(
                        text = stringResource(
                            if (item.isInReview) {
                                R.string.in_review
                            } else {
                                R.string.add_to_review
                            }
                        )
                    )
                }
            )
        }
        
        when (definitionState) {
            null -> {
                Text(
                    text = stringResource(R.string.tap_to_load_definition),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is WordDefinitionState.Loading -> {
                DefinitionShimmer()
            }
            is WordDefinitionState.Loaded -> {
                Text(
                    text = definitionState.definition,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            is WordDefinitionState.Failed -> {
                Text(
                    text = stringResource(R.string.tap_to_retry),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun DefinitionShimmer() {
    val infiniteTransition = rememberInfiniteTransition(label = "def_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "def_shimmer_alpha"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}
