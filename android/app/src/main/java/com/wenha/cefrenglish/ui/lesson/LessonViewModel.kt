package com.wenha.cefrenglish.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.LessonRepository
import com.wenha.cefrenglish.domain.LessonQuestion
import com.wenha.cefrenglish.domain.LessonSubmissionResult
import com.wenha.cefrenglish.domain.WritingReview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LessonUiState(
    val lessonInstanceId: String = "",
    val templateId: String = "",
    val level: String = "",
    val readingText: String = "",
    val readingQuestions: List<LessonQuestion> = emptyList(),
    val grammarExplanation: String = "",
    val grammarQuestions: List<LessonQuestion> = emptyList(),
    val writingPrompt: String = "",
    val writingRubric: List<String> = emptyList(),
    val readingAnswers: List<String> = emptyList(),
    val grammarAnswers: List<String> = emptyList(),
    val writingSubmission: String = "",
    val feedback: WritingReview? = null,
    val submissionResult: LessonSubmissionResult? = null,
    val completed: Boolean = false,
)

class LessonViewModel(private val repository: LessonRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState

    fun loadTodayLesson(userId: String) {
        viewModelScope.launch {
            runCatching { repository.getTodayLesson(userId) }
                .onSuccess { lesson ->
                    _uiState.value = lesson.toUiState()
                }
        }
    }

    fun updateReadingAnswer(index: Int, value: String) {
        val current = _uiState.value
        if (index !in current.readingAnswers.indices) return
        val next = current.readingAnswers.toMutableList()
        next[index] = value
        _uiState.value = current.copy(readingAnswers = next)
    }

    fun updateGrammarAnswer(index: Int, value: String) {
        val current = _uiState.value
        if (index !in current.grammarAnswers.indices) return
        val next = current.grammarAnswers.toMutableList()
        next[index] = value
        _uiState.value = current.copy(grammarAnswers = next)
    }

    fun updateWriting(value: String) {
        _uiState.value = _uiState.value.copy(writingSubmission = value)
    }

    fun regenerate(userId: String) {
        viewModelScope.launch {
            runCatching { repository.regenerateTodayLesson(userId) }
                .onSuccess { lesson ->
                    _uiState.value = lesson.toUiState()
                }
        }
    }

    fun submitLesson(userId: String) {
        val lesson = _uiState.value
        if (lesson.lessonInstanceId.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.submitLesson(
                    userId = userId,
                    lessonInstanceId = lesson.lessonInstanceId,
                    prompt = lesson.writingPrompt,
                    level = lesson.level,
                    readingAnswers = lesson.readingAnswers,
                    grammarAnswers = lesson.grammarAnswers,
                    writingSubmission = lesson.writingSubmission,
                )
            }.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    feedback = result.feedback,
                    submissionResult = result,
                    completed = result.completed,
                )
            }
        }
    }
}

private fun com.wenha.cefrenglish.domain.DailyLesson.toUiState(): LessonUiState =
    LessonUiState(
        lessonInstanceId = lessonInstanceId,
        templateId = templateId,
        level = level,
        readingText = readingText,
        readingQuestions = readingQuestions,
        grammarExplanation = grammarExplanation,
        grammarQuestions = grammarQuestions,
        writingPrompt = writingPrompt,
        writingRubric = writingRubric,
        readingAnswers = List(readingQuestions.size) { "" },
        grammarAnswers = List(grammarQuestions.size) { "" },
    )
