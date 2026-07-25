package com.rudra.defineeasy.feature_dictionary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.feature_dictionary.data.local.ReviewHistoryDao
import com.rudra.defineeasy.feature_dictionary.domain.repository.WordInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: WordInfoRepository,
    private val reviewHistoryDao: ReviewHistoryDao
) : ViewModel() {

    val uiState: StateFlow<ProgressUiState> = combine(
        repository.getFavorites(),
        reviewHistoryDao.getReviewHistory()
    ) { favorites, history ->
        val masteredCount = favorites.count { it.repetitions >= 4 }
        val learningCount = favorites.count { it.repetitions in 1..3 }
        
        ProgressUiState.Success(
            masteredCount = masteredCount,
            learningCount = learningCount,
            reviewHistory = history.take(10).map { it.word } // Example
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState.Loading
    )
}

sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data class Success(
        val masteredCount: Int,
        val learningCount: Int,
        val reviewHistory: List<String>
    ) : ProgressUiState
}
