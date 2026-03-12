package com.rudra.defineeasy.feature_dictionary.presentation.screens

import android.content.Intent
import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.defineeasy.R
import com.rudra.defineeasy.feature_dictionary.domain.model.Meaning
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.presentation.WordDetailViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import kotlinx.coroutines.launch

@Composable
fun WordDetailScreenRoute(
    onBackClick: () -> Unit,
    onWordSelected: (String) -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is WordDetailViewModel.UIEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    WordDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onRetryClick = viewModel::refresh,
        onToggleFavorite = viewModel::toggleFavorite,
        onWordSelected = onWordSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    state: com.rudra.defineeasy.feature_dictionary.presentation.WordDetailState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onWordSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(mediaPlayer) {
        onDispose {
            mediaPlayer.release()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.wordInfo?.word ?: stringResource(R.string.word_not_found),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (state.wordInfo != null) {
                        IconButton(
                            onClick = {
                                shareWord(
                                    context = context,
                                    wordInfo = state.wordInfo
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.share_word)
                            )
                        }
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (state.wordInfo.isFavorited) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = stringResource(
                                    if (state.wordInfo.isFavorited) {
                                        R.string.unfavorite_word
                                    } else {
                                        R.string.favorite_word
                                    }
                                )
                            )
                        }
                        if (state.wordInfo.audioUrl.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    mediaPlayer.reset()
                                    mediaPlayer.setOnPreparedListener { it.start() }
                                    mediaPlayer.setDataSource(state.wordInfo.audioUrl)
                                    mediaPlayer.prepareAsync()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(R.string.play_pronunciation)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.wordInfo == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(horizontal = 24.dp))
                }
            }

            state.wordInfo != null -> {
                WordDetailContent(
                    wordInfo = state.wordInfo,
                    modifier = Modifier.padding(paddingValues),
                    onRelatedWordClick = onWordSelected
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.word_not_found),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.word_not_found_message))
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onRetryClick) {
                        Text(text = stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun WordDetailContent(
    wordInfo: WordInfo,
    modifier: Modifier = Modifier,
    onRelatedWordClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = wordInfo.word,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (wordInfo.phonetic.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = wordInfo.phonetic,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (wordInfo.origin.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.origin_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = wordInfo.origin)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        items(wordInfo.meanings) { meaning ->
            MeaningSection(
                meaning = meaning,
                onRelatedWordClick = onRelatedWordClick
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeaningSection(
    meaning: Meaning,
    onRelatedWordClick: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = meaning.partOfSpeech,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(modifier = Modifier.height(12.dp))

    meaning.definitions.forEachIndexed { index, definition ->
        Text(
            text = "${index + 1}. ${definition.definition}",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        definition.example?.takeIf { it.isNotBlank() }?.let { example ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.example_prefix, example),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        val synonyms = definition.synonyms.distinct()
        if (synonyms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.synonyms),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            ChipRow(words = synonyms, onWordSelected = onRelatedWordClick)
        }

        val antonyms = definition.antonyms.distinct()
        if (antonyms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.antonyms),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            ChipRow(words = antonyms, onWordSelected = onRelatedWordClick)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ChipRow(
    words: List<String>,
    onWordSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(words) { word ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable { onWordSelected(word) }
            ) {
                Text(
                    text = word,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun shareWord(
    context: android.content.Context,
    wordInfo: WordInfo
) {
    val definition = wordInfo.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition.orEmpty()
    val shareText = context.getString(R.string.share_text, wordInfo.word, definition)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    startActivity(
        context,
        Intent.createChooser(shareIntent, context.getString(R.string.share_chooser_title)),
        null
    )
}
