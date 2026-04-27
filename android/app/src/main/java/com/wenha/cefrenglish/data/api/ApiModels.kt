package com.wenha.cefrenglish.data.api

data class PlacementAnswerDto(val skill: String, val correct: Int, val total: Int)

data class PlacementRequestDto(
    val userId: String,
    val answers: List<PlacementAnswerDto>,
    val writingWordCount: Int,
)

data class PlacementResultDto(val level: String, val weaknesses: List<String>)

data class LessonQuestionDto(
    val questionId: String,
    val prompt: String,
    val choices: List<String>,
    val answer: String,
)

data class NextLessonRequestDto(val userId: String, val level: String)

data class TodayLessonRegenerateRequestDto(val userId: String)

data class DailyLessonDto(
    val lessonInstanceId: String,
    val templateId: String,
    val level: String,
    val theme: String,
    val readingText: String,
    val readingQuestions: List<LessonQuestionDto>,
    val grammarExplanation: String,
    val grammarQuestions: List<LessonQuestionDto>,
    val writingPrompt: String,
    val writingRubric: List<String>,
    val generationVersion: Int,
)

data class LessonDto(
    val lessonId: String,
    val level: String,
    val readingText: String,
    val grammarExplanation: String,
    val writingPrompt: String,
)

data class WritingReviewRequestDto(
    val userId: String,
    val lessonId: String,
    val level: String,
    val prompt: String,
    val submission: String,
)

data class WritingRuleChecksDto(
    val notBlank: Boolean,
    val minSentencesOk: Boolean,
    val onTopicLikely: Boolean,
)

data class WritingFeedbackDto(
    val grammar: String,
    val vocabulary: String,
    val coherence: String,
    val suggestions: List<String>,
    val rewrite: String,
)

data class WritingReviewResultDto(
    val ruleChecks: WritingRuleChecksDto,
    val feedback: WritingFeedbackDto,
)

data class CompleteLessonRequestDto(
    val userId: String,
    val level: String,
    val lessonId: String,
)

data class CompleteLessonResultDto(
    val completedLessonIds: List<String>,
    val nextLessonId: String?,
)

data class LessonSubmitRequestDto(
    val userId: String,
    val lessonInstanceId: String,
    val prompt: String,
    val level: String,
    val readingAnswers: List<String>,
    val grammarAnswers: List<String>,
    val writingSubmission: String,
)

data class LessonSubmitResultDto(
    val completed: Boolean,
    val currentLessonId: String,
    val nextLessonId: String?,
    val todayCompleted: Boolean,
    val missingRequirements: List<String>,
    val ruleChecks: WritingRuleChecksDto,
    val feedback: WritingFeedbackDto,
)

data class ProgressSummaryDto(
    val currentLevel: String,
    val currentLessonId: String?,
    val completedCount: Int,
    val nextLessonId: String?,
    val todayCompleted: Boolean,
    val recentWeaknesses: List<String>,
)
