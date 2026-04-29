package com.wenha.cefrenglish.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.LessonRepository
import com.wenha.cefrenglish.domain.DialogueLine
import com.wenha.cefrenglish.domain.KeySentence
import com.wenha.cefrenglish.domain.LessonQuestion
import com.wenha.cefrenglish.domain.LessonSubmissionResult
import com.wenha.cefrenglish.domain.VocabularyItem
import com.wenha.cefrenglish.domain.WritingReview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LessonUiState(
    val lessonInstanceId: String = "",
    val templateId: String = "",
    val level: String = "",
    val theme: String = "",
    val objectives: List<String> = emptyList(),
    val warmupQuestions: List<String> = emptyList(),
    val vocabulary: List<VocabularyItem> = emptyList(),
    val keySentences: List<KeySentence> = emptyList(),
    val dialogue: List<DialogueLine> = emptyList(),
    val speakingPractice: List<String> = emptyList(),
    val listeningPractice: List<String> = emptyList(),
    val reviewTasks: List<String> = emptyList(),
    val readingText: String = "",
    val readingQuestions: List<LessonQuestion> = emptyList(),
    val grammarExplanation: String = "",
    val grammarQuestions: List<LessonQuestion> = emptyList(),
    val writingPrompt: String = "",
    val writingRubric: List<String> = emptyList(),
    val readingAnswers: List<String> = emptyList(),
    val grammarAnswers: List<String> = emptyList(),
    val writingSubmission: String = "",
    val writingRevision: String = "",
    val feedback: WritingReview? = null,
    val submissionResult: LessonSubmissionResult? = null,
    val completed: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val lastLoadRequest: LessonLoadRequest? = null,
)

data class LessonLoadRequest(
    val userId: String,
    val level: String? = null,
    val moduleIndex: Int? = null,
)

class LessonViewModel(private val repository: LessonRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState

    fun loadTodayLesson(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                lastLoadRequest = LessonLoadRequest(userId = userId),
            )
            runCatching { repository.getTodayLesson(userId) }
                .onSuccess { lesson ->
                    _uiState.value = lesson.toUiState().copy(lastLoadRequest = LessonLoadRequest(userId = userId))
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "无法加载课程，请检查网络后重试。",
                    )
                }
        }
    }

    fun loadSyllabusLesson(userId: String, level: String, moduleIndex: Int) {
        viewModelScope.launch {
            val request = LessonLoadRequest(userId = userId, level = level, moduleIndex = moduleIndex)
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, lastLoadRequest = request)
            runCatching { repository.getSyllabusLesson(userId, level, moduleIndex) }
                .onSuccess { lesson ->
                    _uiState.value = lesson.toUiState().copy(lastLoadRequest = request)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "无法加载章节课程，请检查网络后重试。",
                    )
                }
        }
    }

    fun retryLastLoad() {
        val request = _uiState.value.lastLoadRequest ?: return
        if (request.level != null && request.moduleIndex != null) {
            loadSyllabusLesson(request.userId, request.level, request.moduleIndex)
        } else {
            loadTodayLesson(request.userId)
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

    fun updateWritingRevision(value: String) {
        _uiState.value = _uiState.value.copy(writingRevision = value)
    }

    fun regenerate(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { repository.regenerateTodayLesson(userId) }
                .onSuccess { lesson ->
                    _uiState.value = lesson.toUiState().copy(lastLoadRequest = LessonLoadRequest(userId = userId))
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "无法重新生成课程，请稍后再试。",
                    )
                }
        }
    }

    fun submitLesson(userId: String) {
        val lesson = _uiState.value
        if (lesson.lessonInstanceId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            runCatching {
                repository.submitLesson(
                    userId = userId,
                    lessonInstanceId = lesson.lessonInstanceId,
                    prompt = lesson.writingPrompt,
                    level = lesson.level,
                    readingAnswers = lesson.readingAnswers,
                    grammarAnswers = lesson.grammarAnswers,
                    writingSubmission = lesson.writingSubmission,
                    writingRevision = lesson.writingRevision.takeIf { lesson.submissionResult?.revisionRequired == true },
                )
            }.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    feedback = result.feedback,
                    submissionResult = result,
                    completed = result.completed,
                    isSubmitting = false,
                    errorMessage = null,
                )
            }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "提交失败，请检查网络后重试。",
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
        theme = theme,
        objectives = objectives,
        warmupQuestions = warmupQuestions,
        vocabulary = vocabulary,
        keySentences = keySentences,
        dialogue = dialogue,
        speakingPractice = speakingPractice,
        listeningPractice = listeningPractice,
        reviewTasks = reviewTasks,
        readingText = readingText,
        readingQuestions = readingQuestions,
        grammarExplanation = grammarExplanation,
        grammarQuestions = grammarQuestions,
        writingPrompt = writingPrompt,
        writingRubric = writingRubric,
        readingAnswers = List(readingQuestions.size) { "" },
        grammarAnswers = List(grammarQuestions.size) { "" },
        isLoading = false,
        errorMessage = null,
    )
