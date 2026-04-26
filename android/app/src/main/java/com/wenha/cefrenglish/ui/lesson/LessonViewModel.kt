package com.wenha.cefrenglish.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.LessonRepository
import com.wenha.cefrenglish.domain.WritingReview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LessonUiState(
    val lessonId: String = "",
    val readingText: String = "",
    val grammarExplanation: String = "",
    val writingPrompt: String = "",
    val feedback: WritingReview? = null,
)

class LessonViewModel(private val repository: LessonRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState

    fun loadLesson(userId: String, level: String) {
        viewModelScope.launch {
            val lesson = repository.getNextLesson(userId, level)
            _uiState.value = LessonUiState(
                lessonId = lesson.lessonId,
                readingText = lesson.readingText,
                grammarExplanation = lesson.grammarExplanation,
                writingPrompt = lesson.writingPrompt,
                feedback = _uiState.value.feedback,
            )
        }
    }

    fun submitWriting(userId: String, level: String, submission: String) {
        val lesson = _uiState.value
        if (lesson.lessonId.isBlank()) return
        viewModelScope.launch {
            val feedback = repository.submitWriting(
                userId = userId,
                lessonId = lesson.lessonId,
                level = level,
                prompt = lesson.writingPrompt,
                submission = submission,
            )
            _uiState.value = _uiState.value.copy(feedback = feedback)
        }
    }

    fun completeLesson(userId: String, level: String) {
        val lessonId = _uiState.value.lessonId
        if (lessonId.isBlank()) return
        viewModelScope.launch {
            repository.completeLesson(userId, level, lessonId)
        }
    }
}
