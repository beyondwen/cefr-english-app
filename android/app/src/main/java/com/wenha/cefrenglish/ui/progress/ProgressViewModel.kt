package com.wenha.cefrenglish.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProgressUiState(
    val completedCount: Int = 0,
    val nextLessonId: String = "",
    val currentLevel: String = "",
    val currentLessonId: String = "",
    val todayCompleted: Boolean = false,
    val recentWeaknesses: List<String> = emptyList(),
)

class ProgressViewModel(private val repository: ProgressRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState

    fun refresh(userId: String) {
        viewModelScope.launch {
            val result = repository.fetchSummary(userId)
            _uiState.value = ProgressUiState(
                completedCount = result.completedCount,
                nextLessonId = result.nextLessonId.orEmpty(),
                currentLevel = result.currentLevel,
                currentLessonId = result.currentLessonId.orEmpty(),
                todayCompleted = result.todayCompleted,
                recentWeaknesses = result.recentWeaknesses,
            )
        }
    }
}
