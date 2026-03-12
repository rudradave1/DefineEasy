package com.rudra.defineeasy.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.defineeasy.feature_dictionary.domain.use_case.GetDueReviewCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ReviewBadgeViewModel @Inject constructor(
    getDueReviewCountUseCase: GetDueReviewCountUseCase
) : ViewModel() {
    val dueCount: StateFlow<Int> = getDueReviewCountUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
