package com.wenha.cefrenglish.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentLevel: String = "",
    val currentLessonId: String? = null,
    val completedCount: Int = 0,
    val nextLessonId: String? = null,
    val todayCompleted: Boolean = false,
    val recentWeaknesses: List<String> = emptyList(),
) {
    val shouldStartPlacement: Boolean
        get() = currentLessonId == null && completedCount == 0
}

class HomeViewModel(
    private val repository: ProgressRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun refresh(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val summary = repository.fetchSummary(userId)
            _uiState.value = HomeUiState(
                currentLevel = summary.currentLevel,
                currentLessonId = summary.currentLessonId,
                completedCount = summary.completedCount,
                nextLessonId = summary.nextLessonId,
                todayCompleted = summary.todayCompleted,
                recentWeaknesses = summary.recentWeaknesses,
            )
        }
    }
}
