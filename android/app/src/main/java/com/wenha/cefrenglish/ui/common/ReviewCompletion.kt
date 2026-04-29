package com.wenha.cefrenglish.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.ProgressRepository
import kotlinx.coroutines.launch

internal fun ViewModel.launchReviewCompletion(
    userId: String,
    reviewId: String,
    repository: ProgressRepository,
    setCompletingReviewId: (String?) -> Unit,
    setErrorMessage: (String) -> Unit,
    refresh: (String) -> Unit,
) {
    if (userId.isBlank()) return
    viewModelScope.launch {
        setCompletingReviewId(reviewId)
        runCatching { repository.completeReview(userId, reviewId) }
            .onSuccess { refresh(userId) }
            .onFailure {
                setCompletingReviewId(null)
                setErrorMessage("无法更新复习状态，请稍后重试。")
            }
    }
}
