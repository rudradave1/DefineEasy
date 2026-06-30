package com.rudra.defineeasy.feature_dictionary.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.defineeasy.R
import com.rudra.defineeasy.feature_dictionary.domain.model.QuizQuestion
import com.rudra.defineeasy.feature_dictionary.domain.model.QuizState
import com.rudra.defineeasy.feature_dictionary.presentation.QuizViewModel
import com.rudra.defineeasy.ui.theme.DefineEasyTheme
import com.rudra.defineeasy.ui.theme.EasyGreen
import com.rudra.defineeasy.ui.theme.HardRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateUp: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.quiz_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { paddingValues ->
        QuizScreenContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onAnswerSelected = viewModel::answerQuestion,
            onNextQuestion = viewModel::nextQuestion,
            onRestart = viewModel::restartQuiz,
            onNavigateUp = onNavigateUp
        )
    }
}

@Composable
private fun QuizScreenContent(
    uiState: QuizState,
    modifier: Modifier = Modifier,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onRestart: () -> Unit,
    onNavigateUp: () -> Unit
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

        uiState.isComplete -> {
            QuizResultScreen(
                score = uiState.score,
                total = uiState.totalQuestions,
                modifier = modifier,
                onRestart = onRestart,
                onNavigateUp = onNavigateUp
            )
        }

        uiState.currentQuestion != null -> {
            QuizQuestionScreen(
                uiState = uiState,
                modifier = modifier,
                onAnswerSelected = onAnswerSelected,
                onNextQuestion = onNextQuestion
            )
        }

        else -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.quiz_no_questions),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun QuizQuestionScreen(
    uiState: QuizState,
    modifier: Modifier = Modifier,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit
) {
    val question = uiState.currentQuestion ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.quiz_header),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.quiz_progress, uiState.currentIndex + 1, uiState.totalQuestions),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = question.word,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.quiz_pick_definition),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, option ->
                val isSelected = uiState.answeredIndex == index
                val isCorrect = index == question.correctIndex
                val showResult = uiState.answeredIndex != null

                val containerColor = when {
                    showResult && isCorrect -> EasyGreen.copy(alpha = 0.16f)
                    showResult && isSelected && !isCorrect -> HardRed.copy(alpha = 0.16f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }

                val contentColor = when {
                    showResult && isCorrect -> EasyGreen
                    showResult && isSelected && !isCorrect -> HardRed
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }

                OutlinedButton(
                    onClick = { if (!showResult) onAnswerSelected(index) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !showResult
                ) {
                    Text(
                        text = option,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                        color = contentColor
                    )
                }
            }
        }

        if (uiState.answeredIndex != null) {
            Button(
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(
                        if (uiState.currentIndex + 1 >= uiState.totalQuestions) R.string.quiz_finish
                        else R.string.quiz_next
                    )
                )
            }
        }
    }
}

@Composable
private fun QuizResultScreen(
    score: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onRestart: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val percentage = if (total > 0) (score * 100) / total else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.quiz_result_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "$score / $total",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.quiz_result_percentage, percentage),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = stringResource(R.string.quiz_try_again))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onNavigateUp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = stringResource(R.string.quiz_go_back))
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun QuizScreenPreview() {
    DefineEasyTheme {
        QuizScreenContent(
            uiState = QuizState(
                questions = listOf(
                    QuizQuestion(
                        word = "lucid",
                        correctDefinition = "Expressed clearly and easy to understand.",
                        options = listOf(
                            "Expressed clearly and easy to understand.",
                            "To make something less severe.",
                            "A large body of water.",
                            "To move quickly."
                        ),
                        correctIndex = 0
                    )
                ),
                currentIndex = 0,
                isLoading = false
            ),
            onAnswerSelected = {},
            onNextQuestion = {},
            onRestart = {},
            onNavigateUp = {}
        )
    }
}
