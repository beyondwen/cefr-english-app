package com.wenha.cefrenglish.data.api

data class PlacementAnswerDto(val skill: String, val correct: Int, val total: Int)

data class PlacementRequestDto(
    val userId: String,
    val answers: List<PlacementAnswerDto>,
    val writingWordCount: Int,
)

data class PlacementResultDto(val level: String, val weaknesses: List<String>)

data class NextLessonRequestDto(val userId: String, val level: String)

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
    val wordCountOk: Boolean,
    val notBlank: Boolean,
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

data class ProgressSummaryDto(
    val currentLevel: String,
    val completedCount: Int,
    val nextLessonId: String?,
)
