package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.feature_dictionary.domain.model.CollectionWord
import com.rudra.defineeasy.feature_dictionary.domain.model.Definition
import com.rudra.defineeasy.feature_dictionary.domain.model.Meaning
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetCollectionWordsUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetFavoritesUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetSavedWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.SaveWordInfoUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

sealed class WordDefinitionState {
    object Loading : WordDefinitionState()
    data class Loaded(val definition: String) : WordDefinitionState()
    object Failed : WordDefinitionState()
}

@HiltViewModel
class CollectionWordsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getFavoritesUseCase: GetFavoritesUseCase,
    private val getCollectionWordsUseCase: GetCollectionWordsUseCase,
    private val getSavedWordInfo: GetSavedWordInfo,
    private val getWordInfo: GetWordInfo,
    private val saveWordInfoUseCase: SaveWordInfoUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val collectionId = savedStateHandle.get<String>("collectionId").orEmpty()
    private val collectionWords = MutableStateFlow<List<CollectionWord>>(emptyList())
    private val _uiState = MutableStateFlow(CollectionWordsUiState(collectionId = collectionId))
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CollectionWordsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        collectionWords
            .combine(getFavoritesUseCase()) { words, favorites ->
                val favoriteWords = favorites.map { it.word.lowercase() }.toSet()
                _uiState.update { state -> 
                    state.copy(
                        isLoading = false,
                        words = words.map { word ->
                            CollectionWordUiModel(
                                word = word,
                                isInReview = favoriteWords.contains(word.word.lowercase())
                            )
                        },
                        errorMessage = null
                    )
                }
            }
            .launchIn(viewModelScope)

        loadWords()
    }

    fun openWord(word: CollectionWord) {
        viewModelScope.launch {
            ensureWordIsSaved(word)
            _eventFlow.emit(CollectionWordsEvent.OpenWordDetail(word.word))
        }
    }

    fun addToReview(word: CollectionWord, alreadyInReview: Boolean) {
        if (alreadyInReview) return
        viewModelScope.launch {
            ensureWordIsSaved(word)
            toggleFavoriteUseCase(word.word)
        }
    }

    fun checkCacheForWord(word: String) {
        if (_uiState.value.definitionStates.containsKey(word)) return
        viewModelScope.launch {
            val savedWord = getSavedWordInfo(word)
            if (savedWord != null) {
                val def = savedWord.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition ?: ""
                _uiState.update { it.copy(definitionStates = it.definitionStates + (word to WordDefinitionState.Loaded(def))) }
            }
        }
    }

    fun loadDefinitionForWord(word: String) {
        val currentState = _uiState.value.definitionStates[word]
        if (currentState is WordDefinitionState.Loading || currentState is WordDefinitionState.Loaded) return
        
        _uiState.update { it.copy(definitionStates = it.definitionStates + (word to WordDefinitionState.Loading)) }
        
        viewModelScope.launch {
            val savedWord = getSavedWordInfo(word)
            if (savedWord != null) {
                val def = savedWord.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition ?: ""
                _uiState.update { it.copy(definitionStates = it.definitionStates + (word to WordDefinitionState.Loaded(def))) }
                return@launch
            }
            
            val fallbackDef = fallbackDefinitions[word.lowercase()]
            if (fallbackDef != null) {
                _uiState.update { it.copy(definitionStates = it.definitionStates + (word to WordDefinitionState.Loaded(fallbackDef))) }
                return@launch
            }
            
            getWordInfo(word).collect { resource ->
                when (resource) {
                    is com.rudra.defineeasy.core.util.Resource.Error -> {
                        _uiState.update { it.copy(definitionStates = it.definitionStates + (word to WordDefinitionState.Failed)) }
                    }
                    is com.rudra.defineeasy.core.util.Resource.Loading -> {
                        // Keep loading state
                    }
                    is com.rudra.defineeasy.core.util.Resource.Success -> {
                        val def = resource.data?.firstOrNull()?.meanings?.firstOrNull()?.definitions?.firstOrNull()?.definition
                        if (def != null) {
                            _uiState.update { it.copy(definitionStates = it.definitionStates + (word to WordDefinitionState.Loaded(def))) }
                        } else {
                            _uiState.update { it.copy(definitionStates = it.definitionStates + (word to WordDefinitionState.Failed)) }
                        }
                    }
                }
            }
        }
    }

    private fun loadWords() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { getCollectionWordsUseCase(collectionId) }
                .onSuccess { words ->
                    collectionWords.value = words
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Unable to load collection"
                    )
                }
        }
    }

    private suspend fun ensureWordIsSaved(word: CollectionWord) {
        if (getSavedWordInfo(word.word) != null) return
        val currentState = _uiState.value.definitionStates[word.word]
        val def = if (currentState is WordDefinitionState.Loaded) currentState.definition else ""
        saveWordInfoUseCase(
            WordInfo(
                audioUrl = "",
                easinessFactor = 2.5,
                intervalDays = 0,
                isFavorited = false,
                meanings = listOf(
                    Meaning(
                        definitions = listOf(
                            Definition(
                                antonyms = emptyList(),
                                definition = def,
                                example = null,
                                synonyms = emptyList()
                            )
                        ),
                        partOfSpeech = ""
                    )
                ),
                nextReviewDateEpochDay = 0,
                origin = word.confusedWith.orEmpty(),
                phonetic = "",
                repetitions = 0,
                word = word.word
            )
        )
    }

    sealed interface CollectionWordsEvent {
        data class OpenWordDetail(val word: String) : CollectionWordsEvent
    }

    companion object {
        private val fallbackDefinitions = mapOf(
            "change management" to "Managing the transition of an organization from its current state to a desired future state.",
            "core competency" to "A defining capability or advantage that distinguishes an organization from its competitors.",
            "key performance indicator" to "A measurable value that demonstrates how effectively a company is achieving key business objectives.",
            "pain point" to "A specific problem that customers or teams experience repeatedly.",
            "value proposition" to "A statement that explains how a product solves a problem and why it is better than alternatives.",
            "due diligence" to "Careful investigation and research done before making a business decision.",
            "action item" to "A specific task assigned to a person with a deadline.",
            "buy-in" to "Agreement and support from stakeholders for a plan or decision.",
            "capacity building" to "Developing skills, resources, and processes to improve performance over time.",
            "exit strategy" to "A plan for leaving a business position or investment in a controlled way."
        )
    }
}

