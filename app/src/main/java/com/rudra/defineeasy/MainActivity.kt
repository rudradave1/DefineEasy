package com.rudra.defineeasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.defineeasy.feature_dictionary.presentation.components.WordInfoItem
import com.rudra.defineeasy.feature_dictionary.presentation.WordInfoViewModel
import com.rudra.defineeasy.feature_dictionary.presentation.components.SearchHistoryComponent
import com.rudra.defineeasy.ui.theme.DefineEasyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DefineEasyTheme {
                val viewModel: WordInfoViewModel = hiltViewModel()
                val state = viewModel.state.value
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                LaunchedEffect(key1 = true) {
                    viewModel.eventFlow.collectLatest { event ->
                        when (event) {
                            is WordInfoViewModel.UIEvent.ShowSnackbar -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        event.message
                                    )
                                }
                            }
                        }
                    }
                }
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    floatingActionButton = {},
                    content = {
                        Box(
                            modifier = Modifier
                                .padding(it)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Box {
                                    TextField(
                                        value = viewModel.searchQuery.value,
                                        onValueChange = viewModel::onSearch,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = {
                                            Text(text = "Search...")
                                        }
                                    )
                                    IconButton(
                                        onClick = viewModel::updateSearchHistory,
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.List,
                                            tint = Color.Black,
                                            contentDescription = "Search History"
                                        )
                                    }
                                }
                                AnimatedVisibility(visible = state.isSearchHistoryVisible) {
                                    SearchHistoryComponent(
                                        words = state.searchHistory,
                                        onClick = { word -> viewModel.onSearch(word) },
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    bottomStart = 8.dp,
                                                    bottomEnd = 8.dp
                                                )
                                            )
                                            .background(Color.LightGray)
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(state.wordInfoItems.size) { i ->
                                        val wordInfo = state.wordInfoItems[i]
                                        if (i > 0) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        WordInfoItem(wordInfo = wordInfo)
                                        if (i < state.wordInfoItems.size - 1) {
                                            Divider()
                                        }
                                    }
                                }
                            }
                            if (state.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                )
            }
        }
    }
}