package com.wenha.cefrenglish.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProgressUiState(
    val completedCount: Int = 0,
    val totalLessonCount: Int = 0,
    val progressRatio: Float = 0f,
    val nextLessonId: String = "",
    val currentLevel: String = "",
    val currentLessonId: String = "",
    val todayCompleted: Boolean = false,
    val recentWeaknesses: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class ProgressViewModel(private val repository: ProgressRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState

    fun refresh(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { repository.fetchSummary(userId) }
                .onSuccess { result ->
                    _uiState.value = ProgressUiState(
                        completedCount = result.completedCount,
                        totalLessonCount = result.totalLessonCount,
                        progressRatio = result.progressRatio,
                        nextLessonId = result.nextLessonId.orEmpty(),
                        currentLevel = result.currentLevel,
                        currentLessonId = result.currentLessonId.orEmpty(),
                        todayCompleted = result.todayCompleted,
                        recentWeaknesses = result.recentWeaknesses,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "无法加载进度，请检查网络后重试。",
                    )
                }
        }
    }
}
