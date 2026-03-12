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

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    getFavoritesUseCase: GetFavoritesUseCase,
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val getCollectionWordsUseCase: GetCollectionWordsUseCase
) : ViewModel() {

    private val collectionSummaries = MutableStateFlow<List<com.rudra.defineeasy.feature_dictionary.domain.model.CollectionSummary>>(emptyList())
    private val collectionWords = MutableStateFlow<Map<String, List<CollectionWord>>>(emptyMap())
    private val _uiState = MutableStateFlow<CollectionsUiState>(CollectionsUiState.Loading)
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()

    init {
        collectionSummaries
            .combine(collectionWords) { summaries, words -> summaries to words }
            .combine(getFavoritesUseCase()) { (summaries, words), favorites ->
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

        loadCollections()
    }

    private fun loadCollections() {
        viewModelScope.launch {
            _uiState.value = CollectionsUiState.Loading
            runCatching {
                val summaries = getCollectionsUseCase()
                val words = summaries.associate { summary ->
                    summary.id to getCollectionWordsUseCase(summary.id)
                }
                summaries to words
            }.onSuccess { (summaries, words) ->
                collectionSummaries.value = summaries
                collectionWords.value = words
            }.onFailure {
                _uiState.value = CollectionsUiState.Error(it.message ?: "Unable to load collections")
            }
        }
    }
}
