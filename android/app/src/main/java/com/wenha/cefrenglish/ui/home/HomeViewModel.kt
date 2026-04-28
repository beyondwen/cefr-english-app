package com.wenha.cefrenglish.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.ProgressRepository
import com.wenha.cefrenglish.data.SyllabusRepository
import com.wenha.cefrenglish.domain.CourseSyllabus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentLevel: String = "",
    val currentLessonId: String? = null,
    val completedCount: Int = 0,
    val totalLessonCount: Int = 0,
    val progressRatio: Float = 0f,
    val nextLessonId: String? = null,
    val todayCompleted: Boolean = false,
    val recentWeaknesses: List<String> = emptyList(),
    val selectedSyllabus: CourseSyllabus? = null,
    val regeneratingLevel: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasLoaded: Boolean = false,
    val lastUserId: String = "",
)

class HomeViewModel(
    private val repository: ProgressRepository,
    private val syllabusRepository: SyllabusRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun refresh(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { repository.fetchSummary(userId) }
                .onSuccess { summary ->
                    _uiState.value = HomeUiState(
                        currentLevel = summary.currentLevel,
                        currentLessonId = summary.currentLessonId,
                        completedCount = summary.completedCount,
                        totalLessonCount = summary.totalLessonCount,
                        progressRatio = summary.progressRatio,
                        nextLessonId = summary.nextLessonId,
                        todayCompleted = summary.todayCompleted,
                        recentWeaknesses = summary.recentWeaknesses,
                        selectedSyllabus = _uiState.value.selectedSyllabus,
                        regeneratingLevel = _uiState.value.regeneratingLevel,
                        isLoading = false,
                        errorMessage = null,
                        hasLoaded = true,
                        lastUserId = userId,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "无法加载学习进度，请检查网络后重试。",
                        hasLoaded = true,
                        lastUserId = userId,
                    )
                }
        }
    }

    fun retryRefresh() {
        refresh(_uiState.value.lastUserId)
    }

    fun loadSyllabus(level: String) {
        viewModelScope.launch {
            runCatching { syllabusRepository.fetchSyllabus(level) }
                .onSuccess { syllabus ->
                    _uiState.value = _uiState.value.copy(selectedSyllabus = syllabus, errorMessage = null)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = "无法加载 $level 课程大纲，已显示本地备用大纲。")
                }
        }
    }

    fun regenerateSyllabus(level: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(regeneratingLevel = level)
            runCatching { syllabusRepository.regenerateSyllabus(level) }
                .onSuccess { syllabus ->
                    _uiState.value = _uiState.value.copy(
                        selectedSyllabus = syllabus,
                        regeneratingLevel = null,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(regeneratingLevel = null)
                }
        }
    }
}
