package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetCollectionWordsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetCollectionsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import com.rudra.defineeasy.core.analytics.AnalyticsService
import com.rudra.defineeasy.feature_dictionary.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    getFavoritesUseCase: GetFavoritesUseCase,
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val getCollectionWordsUseCase: GetCollectionWordsUseCase,
    private val collectionRepository: CollectionRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val collectionSummaries = MutableStateFlow<List<com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary>>(emptyList())
    private val collectionWords = MutableStateFlow<Map<String, List<CollectionWord>>>(emptyMap())
    private val _uiState = MutableStateFlow<CollectionsUiState>(CollectionsUiState.Loading)
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()

    init {
        collectionRepository.getCustomCollections()
            .combine(flowOf(true)) { customSummaries, _ ->
                // Refresh static collections and merge with custom
                loadCollections(customSummaries)
            }
            .launchIn(viewModelScope)

        combine(
            collectionSummaries,
            collectionWords,
            getFavoritesUseCase()
        ) { summaries, words, favorites ->
            if (summaries.isEmpty()) {
                CollectionsUiState.Empty
            } else {
                val favoriteWords = favorites.map { it.word.lowercase() }.toSet()
                CollectionsUiState.Success(
                    collections = summaries.map { summary ->
                        val wordsInCollection = words[summary.id].orEmpty()
                        val completedCount = wordsInCollection.count { collectionWord ->
                            favoriteWords.contains(collectionWord.word.lowercase())
                        }
                        val completionPercentage = if (summary.wordCount == 0) {
                            0
                        } else {
                            (completedCount * 100) / summary.wordCount
                        }
                        CollectionCardUiModel(
                            id = summary.id,
                            wordCount = summary.wordCount,
                            completionPercentage = completionPercentage
                        )
                    }
                )
            }
        }
        .onEach { state ->
            if (_uiState.value !is CollectionsUiState.Error) {
                _uiState.value = state
            }
        }
        .launchIn(viewModelScope)
    }

    private fun loadCollections(customSummaries: List<com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary> = emptyList()) {
        viewModelScope.launch {
            runCatching {
                val staticSummaries = getCollectionsUseCase()
                val allSummaries = staticSummaries + customSummaries
                val words = allSummaries.associate { summary ->
                    summary.id to getCollectionWordsUseCase(summary.id)
                }
                allSummaries to words
            }.onSuccess { (summaries, words) ->
                collectionSummaries.value = summaries
                collectionWords.value = words
            }.onFailure {
                _uiState.value = CollectionsUiState.Error(it.message ?: "Unable to load collections")
            }
        }
    }

    fun refresh() {
        loadCollections()
    }

    fun onEvent(event: CollectionsEvent) {
        when (event) {
            is CollectionsEvent.CreateCollection -> {
                viewModelScope.launch {
                    collectionRepository.createCustomCollection(event.name)
                    analyticsService.onCollectionCreated()
                }
            }
            is CollectionsEvent.DeleteCollection -> {
                viewModelScope.launch {
                    collectionRepository.deleteCustomCollection(event.id)
                }
            }
        }
    }
}
