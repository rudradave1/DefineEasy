package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.feature_dictionary.data.local.WordOfDayPreferences
import com.rudra.defineeasy.feature_dictionary.domain.model.Definition
import com.rudra.defineeasy.feature_dictionary.domain.model.Meaning
import com.rudra.defineeasy.feature_dictionary.domain.model.WordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetSavedWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetWordInfo
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetWordOfDayUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.SaveWordInfoUseCase
import com.rudra.defineeasy.feature_dictionary.domain.use_case.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.LocalDate
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel
class WordOfDayViewModel @Inject constructor(
    private val wordOfDayPreferences: WordOfDayPreferences,
    private val getWordOfDayUseCase: GetWordOfDayUseCase,
    private val getSavedWordInfo: GetSavedWordInfo,
    private val getWordInfo: GetWordInfo,
    private val saveWordInfoUseCase: SaveWordInfoUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = androidx.compose.runtime.mutableStateOf(WordOfDayUiState())
    val uiState: androidx.compose.runtime.State<WordOfDayUiState> = _uiState

    init {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            if (wordOfDayPreferences.getShownDate() == today) {
                return@launch
            }
            val wordOfDay = getWordOfDayUseCase()
            val isFavorited = getSavedWordInfo(wordOfDay.word)?.isFavorited == true
            _uiState.value = WordOfDayUiState(
                wordOfDay = wordOfDay,
                isVisible = true,
                isFavorited = isFavorited
            )
        }
    }

    fun dismiss() {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            wordOfDayPreferences.setShownDate(today)
            _uiState.value = _uiState.value.copy(isVisible = false)
        }
    }

    fun toggleFavorite() {
        val currentWordOfDay = _uiState.value.wordOfDay ?: return
        val word = currentWordOfDay.word
        viewModelScope.launch {
            if (getSavedWordInfo(word) == null) {
                getWordInfo(word).firstOrNull()
            }
            if (getSavedWordInfo(word) == null) {
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
                                        definition = currentWordOfDay.definition,
                                        example = null,
                                        synonyms = emptyList()
                                    )
                                ),
                                partOfSpeech = ""
                            )
                        ),
                        nextReviewDateEpochDay = 0,
                        origin = "",
                        phonetic = currentWordOfDay.phonetic,
                        repetitions = 0,
                        word = currentWordOfDay.word
                    )
                )
            }
            toggleFavoriteUseCase(word)
            val isFavorited = getSavedWordInfo(word)?.isFavorited == true
            _uiState.value = _uiState.value.copy(isFavorited = isFavorited)
        }
    }
}
