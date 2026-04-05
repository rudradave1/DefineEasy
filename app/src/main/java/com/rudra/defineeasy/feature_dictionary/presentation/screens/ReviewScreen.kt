package com.rudra.defineeasy.feature_dictionary.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.defineeasy.R
import com.rudra.defineeasy.feature_dictionary.domain.model.Definition
import com.rudra.defineeasy.feature_dictionary.domain.model.Meaning
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.presentation.ReviewUiState
import com.rudra.defineeasy.feature_dictionary.presentation.ReviewViewModel
import com.rudra.defineeasy.ui.theme.DefineEasyTheme
import com.rudra.defineeasy.ui.theme.EasyGreen
import com.rudra.defineeasy.ui.theme.GoodAmber
import com.rudra.defineeasy.ui.theme.HardRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onNavigateUp: () -> Unit = {},
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    BackHandler(onBack = onNavigateUp)

    Scaffold { paddingValues ->
        ReviewScreenContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onToggleAnswerVisibility = viewModel::toggleAnswerVisibility,
            onRateAgain = { viewModel.rateCurrentWord(0) },
            onRateHard = { viewModel.rateCurrentWord(1) },
            onRateGood = { viewModel.rateCurrentWord(3) },
            onRateEasy = { viewModel.rateCurrentWord(5) }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ReviewScreenContent(
    uiState: ReviewUiState,
    modifier: Modifier = Modifier,
    onToggleAnswerVisibility: () -> Unit,
    onRateAgain: () -> Unit,
    onRateHard: () -> Unit,
    onRateGood: () -> Unit,
    onRateEasy: () -> Unit
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.currentWord == null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.all_caught_up),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        else -> {
            val currentWord = uiState.currentWord ?: return
            val totalCards = uiState.dueWords.size.coerceAtLeast(1)
            val progress = (uiState.completedCount.toFloat() / totalCards.toFloat()).coerceIn(0f, 1f)

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.review_header_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.review_header_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.review_progress,
                            uiState.completedCount + 1,
                            uiState.dueWords.size
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedContent(
                    targetState = currentWord.word,
                    transitionSpec = {
                        (slideInHorizontally(
                            animationSpec = tween(320),
                            initialOffsetX = { it / 3 }
                        ) + fadeIn(animationSpec = tween(280)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = tween(250),
                                    targetOffsetX = { -it / 5 }
                                ) + fadeOut(animationSpec = tween(200))
                            )
                            .using(SizeTransform(clip = false))
                    },
                    label = "review_card_transition"
                ) {
                    ReviewFlashCard(
                        wordInfo = currentWord,
                        isAnswerVisible = uiState.isAnswerVisible,
                        onClick = onToggleAnswerVisibility,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                if (uiState.isAnswerVisible) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onRateAgain,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = stringResource(R.string.review_again_secondary))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RatingButton(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.review_hard),
                                containerColor = HardRed.copy(alpha = 0.16f),
                                contentColor = HardRed,
                                onClick = onRateHard
                            )
                            RatingButton(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.review_good),
                                containerColor = GoodAmber.copy(alpha = 0.18f),
                                contentColor = GoodAmber,
                                onClick = onRateGood
                            )
                            RatingButton(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.review_easy),
                                containerColor = EasyGreen.copy(alpha = 0.16f),
                                contentColor = EasyGreen,
                                onClick = onRateEasy
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewFlashCard(
    wordInfo: WordInfo,
    isAnswerVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isAnswerVisible) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "review_card_rotation"
    )
    val primaryMeaning = wordInfo.meanings.firstOrNull()
    val primaryDefinition = primaryMeaning?.definitions?.firstOrNull()

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            },
        shape = RoundedCornerShape(28.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = wordInfo.word,
                        textAlign = TextAlign.Center,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.tap_to_flip),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    primaryMeaning?.partOfSpeech?.takeIf { it.isNotBlank() }?.let { speech ->
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = speech,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = primaryDefinition?.definition.orEmpty(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    primaryDefinition?.example?.takeIf { it.isNotBlank() }?.let { example ->
                        Text(
                            text = stringResource(R.string.example_prefix, example),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingButton(
    modifier: Modifier = Modifier,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, heightDp = 820)
@Composable
private fun ReviewScreenPreview() {
    DefineEasyTheme {
        ReviewScreenContent(
            uiState = ReviewUiState(
                dueWords = listOf(
                    WordInfo(
                        word = "lucid",
                        phonetic = "",
                        origin = "",
                        meanings = listOf(
                            Meaning(
                                partOfSpeech = "adjective",
                                definitions = listOf(
                                    Definition(
                                        definition = "Expressed clearly and easy to understand.",
                                        example = "Her explanation of fiscal policy was lucid and precise.",
                                        synonyms = emptyList(),
                                        antonyms = emptyList()
                                    )
                                )
                            )
                        ),
                        audioUrl = "",
                        isFavorited = true,
                        intervalDays = 1,
                        repetitions = 1,
                        easinessFactor = 2.5,
                        nextReviewDateEpochDay = 0
                    )
                ),
                isAnswerVisible = true,
                isLoading = false
            ),
            onToggleAnswerVisibility = {},
            onRateAgain = {},
            onRateHard = {},
            onRateGood = {},
            onRateEasy = {}
        )
    }
}
