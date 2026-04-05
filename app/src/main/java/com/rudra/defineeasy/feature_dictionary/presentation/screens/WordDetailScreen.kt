package com.rudra.defineeasy.feature_dictionary.presentation.screens

import android.content.Intent
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.defineeasy.R
import com.rudra.defineeasy.feature_dictionary.domain.model.Definition
import com.rudra.defineeasy.feature_dictionary.domain.model.Meaning
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.presentation.WordDetailState
import com.rudra.defineeasy.feature_dictionary.presentation.WordDetailViewModel
import com.rudra.defineeasy.ui.theme.DefineEasyTheme
import com.rudra.defineeasy.ui.theme.PartOfSpeechAdjective
import com.rudra.defineeasy.ui.theme.PartOfSpeechAdverb
import com.rudra.defineeasy.ui.theme.PartOfSpeechDefault
import com.rudra.defineeasy.ui.theme.PartOfSpeechNoun
import com.rudra.defineeasy.ui.theme.PartOfSpeechVerb
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun WordDetailScreenRoute(
    onNavigateUp: () -> Unit,
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
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
            }
        }
    }

    WordDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onRetryClick = viewModel::refresh,
        onToggleFavorite = viewModel::toggleFavorite,
        onWordSelected = onWordSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    state: WordDetailState,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onRetryClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onWordSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(mediaPlayer) {
        onDispose { mediaPlayer.release() }
    }
    BackHandler(onBack = onNavigateUp)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.word_not_found)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    state.wordInfo?.let { wordInfo ->
                        IconButton(onClick = { shareWord(context, wordInfo) }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.share_word)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        },
        bottomBar = {
            state.wordInfo?.let { wordInfo ->
                DetailActionBar(
                    isFavorited = wordInfo.isFavorited,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.wordInfo == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.wordInfo != null -> {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 8 }) + fadeIn()
                ) {
                    WordDetailContent(
                        wordInfo = state.wordInfo,
                        modifier = Modifier.padding(paddingValues),
                        onPlayAudio = { playAudio(mediaPlayer, state.wordInfo.audioUrl) },
                        onRelatedWordClick = onWordSelected
                    )
                }
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
    onPlayAudio: () -> Unit,
    onRelatedWordClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wordInfo.word,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (wordInfo.phonetic.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = wordInfo.phonetic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                IconButton(
                    onClick = onPlayAudio,
                    enabled = wordInfo.audioUrl.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = stringResource(R.string.play_pronunciation)
                    )
                }
            }
        }

        items(wordInfo.meanings) { meaning ->
            MeaningCard(
                meaning = meaning,
                onRelatedWordClick = onRelatedWordClick
            )
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@Composable
private fun MeaningCard(
    meaning: Meaning,
    onRelatedWordClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PartOfSpeechChip(partOfSpeech = meaning.partOfSpeech)

            meaning.definitions.forEachIndexed { index, definition ->
                DefinitionCard(
                    index = index + 1,
                    definition = definition,
                    onRelatedWordClick = onRelatedWordClick
                )
            }
        }
    }
}

@Composable
private fun PartOfSpeechChip(partOfSpeech: String) {
    val color = when (partOfSpeech.lowercase()) {
        "noun" -> PartOfSpeechNoun
        "verb" -> PartOfSpeechVerb
        "adjective" -> PartOfSpeechAdjective
        "adverb" -> PartOfSpeechAdverb
        else -> PartOfSpeechDefault
    }
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = partOfSpeech.ifBlank { stringResource(R.string.pronunciation) },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DefinitionCard(
    index: Int,
    definition: Definition,
    onRelatedWordClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "$index. ${definition.definition}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            definition.example?.takeIf { it.isNotBlank() }?.let { example ->
                Text(
                    text = stringResource(R.string.example_prefix, example),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
            if (definition.synonyms.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.synonyms),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                RelatedWordsRow(
                    words = definition.synonyms.distinct(),
                    onWordSelected = onRelatedWordClick
                )
            }
            if (definition.antonyms.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.antonyms),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                RelatedWordsRow(
                    words = definition.antonyms.distinct(),
                    onWordSelected = onRelatedWordClick
                )
            }
        }
    }
}

@Composable
private fun RelatedWordsRow(
    words: List<String>,
    onWordSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(words) { word ->
            AssistChip(
                onClick = { onWordSelected(word) },
                label = { Text(text = word) }
            )
        }
    }
}

@Composable
private fun DetailActionBar(
    isFavorited: Boolean,
    onToggleFavorite: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = onToggleFavorite,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (isFavorited) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(
                        if (isFavorited) R.string.unfavorite_word else R.string.favorite_word
                    )
                )
            }
            Button(
                onClick = onToggleFavorite,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(
                        if (isFavorited) R.string.saved_to_review else R.string.save_to_review
                    )
                )
            }
        }
    }
}

private fun playAudio(mediaPlayer: MediaPlayer, audioUrl: String) {
    if (audioUrl.isBlank()) return
    runCatching {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(audioUrl)
        mediaPlayer.prepare()
        mediaPlayer.start()
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun WordDetailScreenPreview() {
    DefineEasyTheme {
        WordDetailScreen(
            state = WordDetailState(
                wordInfo = WordInfo(
                    word = "lucid",
                    phonetic = "/ˈluː.sɪd/",
                    origin = "",
                    meanings = listOf(
                        Meaning(
                            partOfSpeech = "adjective",
                            definitions = listOf(
                                Definition(
                                    definition = "Expressed clearly and easy to understand.",
                                    example = "Her answer on public finance was lucid and sharply argued.",
                                    synonyms = listOf("clear", "precise", "coherent"),
                                    antonyms = listOf("confusing")
                                )
                            )
                        )
                    ),
                    audioUrl = "",
                    isFavorited = true,
                    intervalDays = 0,
                    repetitions = 0,
                    easinessFactor = 2.5,
                    nextReviewDateEpochDay = 0
                )
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateUp = {},
            onRetryClick = {},
            onToggleFavorite = {},
            onWordSelected = {}
        )
    }
}
