package com.rudra.defineeasy.feature_dictionary.presentation.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.defineeasy.R
import com.rudra.defineeasy.feature_dictionary.domain.model.Definition
import com.rudra.defineeasy.feature_dictionary.domain.model.Meaning
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.domain.model.WordOfDay
import com.rudra.defineeasy.feature_dictionary.presentation.WordInfoState
import com.rudra.defineeasy.feature_dictionary.presentation.WordInfoViewModel
import com.rudra.defineeasy.feature_dictionary.presentation.WordOfDayUiState
import com.rudra.defineeasy.feature_dictionary.presentation.WordOfDayViewModel
import com.rudra.defineeasy.feature_dictionary.presentation.components.SearchHistoryComponent
import com.rudra.defineeasy.feature_dictionary.presentation.components.WordInfoItem
import com.rudra.defineeasy.navigation.ReviewBadgeViewModel
import com.rudra.defineeasy.ui.theme.DefineEasyTheme
import com.rudra.defineeasy.ui.theme.ReviewAmber
import com.rudra.defineeasy.ui.theme.StreakOrange
import com.rudra.defineeasy.ui.theme.WordOfDayEnd
import com.rudra.defineeasy.ui.theme.WordOfDayStart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onWordSelected: (String) -> Unit,
    contentPadding: PaddingValues,
    onOpenSettings: () -> Unit,
    onOpenReview: () -> Unit,
    viewModel: WordInfoViewModel = hiltViewModel(),
    wordOfDayViewModel: WordOfDayViewModel = hiltViewModel(),
    reviewBadgeViewModel: ReviewBadgeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val wordOfDayState = wordOfDayViewModel.uiState.value
    val dueCount by reviewBadgeViewModel.dueCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isOffline = rememberOfflineState(context)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is WordInfoViewModel.UIEvent.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.search_tab),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(contentPadding)
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = isOffline) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.offline_banner),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                SearchHeroSection(
                    wordOfDayState = wordOfDayState,
                    dueCount = dueCount,
                    onOpenWord = { word -> onWordSelected(word) },
                    onOpenReview = onOpenReview
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.search_section_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.search_section_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextField(
                    value = viewModel.searchQuery.value,
                    onValueChange = viewModel::onQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .onFocusChanged { focusState ->
                            viewModel.onSearchFieldFocusChanged(focusState.isFocused)
                        },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() })
                )

                val showDropdown = state.isSearchFieldFocused && viewModel.searchQuery.value.isEmpty()
                BackHandler(enabled = showDropdown) {
                    focusManager.clearFocus()
                }

                AnimatedVisibility(visible = showDropdown) {
                    SearchHistoryComponent(
                        words = state.searchHistory,
                        onClick = { word ->
                            focusManager.clearFocus()
                            viewModel.onSearchHistoryItemSelected(word)
                        },
                        onDelete = viewModel::deleteSearchHistoryItem,
                        onClearAll = viewModel::clearSearchHistory,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }

                when {
                    state.isLoading -> {
                        SearchShimmer(modifier = Modifier.fillMaxSize())
                    }

                    state.wordInfoItems.isNotEmpty() -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.wordInfoItems.size) { index ->
                                val wordInfo = state.wordInfoItems[index]
                                if (index > 0) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                WordInfoItem(
                                    wordInfo = wordInfo,
                                    onClick = { onWordSelected(wordInfo.word) }
                                )
                                if (index < state.wordInfoItems.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
                                }
                            }
                        }
                    }

                    state.hasSearched && viewModel.searchQuery.value.isNotBlank() -> {
                        EmptySearchState(query = viewModel.searchQuery.value)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeroSection(
    wordOfDayState: WordOfDayUiState,
    dueCount: Int,
    onOpenWord: (String) -> Unit,
    onOpenReview: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WordOfDayHeroCard(
            wordOfDay = wordOfDayState.wordOfDay,
            onOpenWord = { wordOfDayState.wordOfDay?.let { onOpenWord(it.word) } }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StreakCard(
                modifier = Modifier.weight(1f),
                streakCount = 0,
                onClick = onOpenReview
            )
            AnimatedVisibility(visible = dueCount > 0) {
                DueReviewBadge(
                    modifier = Modifier.weight(1f),
                    dueCount = dueCount,
                    onClick = onOpenReview
                )
            }
        }
    }
}

@Composable
private fun WordOfDayHeroCard(
    wordOfDay: WordOfDay?,
    onOpenWord: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenWord),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(listOf(WordOfDayStart, WordOfDayEnd))
                )
                .padding(20.dp)
        ) {
            if (wordOfDay == null) {
                WordOfDayCardLoading()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.word_of_day_label),
                        color = Color.White.copy(alpha = 0.68f),
                        letterSpacing = 1.4.sp,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = wordOfDay.word,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = wordOfDay.definition,
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onOpenWord) {
                            Text(
                                text = stringResource(R.string.learn_word_of_day),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordOfDayCardLoading() {
    val transition = rememberInfiniteTransition(label = "word_of_day_loading")
    val shimmerAlpha by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "word_of_day_alpha"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.word_of_day_label),
            color = Color.White.copy(alpha = 0.68f),
            letterSpacing = 1.4.sp,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == 0) 0.42f else if (index == 1) 0.84f else 0.68f)
                    .height(if (index == 0) 34.dp else 16.dp)
                    .graphicsLayer { alpha = shimmerAlpha }
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
private fun StreakCard(
    modifier: Modifier = Modifier,
    streakCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StreakOrange.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔥", fontSize = 18.sp)
            }
            Text(
                text = if (streakCount > 0) {
                    stringResource(R.string.streak_card_count, streakCount)
                } else {
                    stringResource(R.string.streak_card_zero)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DueReviewBadge(
    modifier: Modifier = Modifier,
    dueCount: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = ReviewAmber.copy(alpha = 0.16f),
        contentColor = ReviewAmber,
        shape = RoundedCornerShape(999.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.due_reviews_pill, dueCount),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.no_results_for_word, query),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.spelling_suggestion_prompt),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchShimmer(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "search_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "search_shimmer_alpha"
    )

    LazyColumn(modifier = modifier) {
        items(4) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .graphicsLayer { this.alpha = alpha },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberOfflineState(context: Context): Boolean {
    var isOffline by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOffline = false
            }

            override fun onLost(network: Network) {
                isOffline = !connectivityManager.isCurrentlyOnline()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                isOffline = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        isOffline = !connectivityManager.isCurrentlyOnline()
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback
        )
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
    return isOffline
}

private fun ConnectivityManager.isCurrentlyOnline(): Boolean {
    val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Preview(showBackground = true)
@Composable
private fun WordOfDayHeroCardPreview() {
    DefineEasyTheme {
        WordOfDayHeroCard(
            wordOfDay = WordOfDay(
                word = "sagacious",
                phonetic = "",
                definition = "Showing good judgment and a calm understanding of what matters."
            ),
            onOpenWord = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchHeroSectionPreview() {
    DefineEasyTheme {
        SearchHeroSection(
            wordOfDayState = WordOfDayUiState(
                wordOfDay = WordOfDay(
                    word = "lucid",
                    phonetic = "",
                    definition = "Expressed clearly enough to be understood at once."
                )
            ),
            dueCount = 6,
            onOpenWord = {},
            onOpenReview = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptySearchStatePreview() {
    DefineEasyTheme {
        EmptySearchState(query = "abrogate")
    }
}
