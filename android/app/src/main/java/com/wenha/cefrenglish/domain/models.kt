package com.wenha.cefrenglish.domain

data class PlacementResult(val level: String, val weaknesses: List<String>)

data class LessonQuestion(
    val questionId: String,
    val prompt: String,
    val choices: List<String>,
    val answer: String,
)

data class DailyLesson(
    val lessonInstanceId: String,
    val templateId: String,
    val level: String,
    val theme: String,
    val readingText: String,
    val readingQuestions: List<LessonQuestion>,
    val grammarExplanation: String,
    val grammarQuestions: List<LessonQuestion>,
    val writingPrompt: String,
    val writingRubric: List<String>,
    val generationVersion: Int,
)

data class Lesson(
    val lessonId: String,
    val level: String,
    val readingText: String,
    val grammarExplanation: String,
    val writingPrompt: String,
)

data class WritingReview(
    val grammar: String,
    val vocabulary: String,
    val coherence: String,
    val suggestions: List<String>,
    val rewrite: String,
)

data class WritingRuleChecks(
    val notBlank: Boolean,
    val minSentencesOk: Boolean,
    val onTopicLikely: Boolean,
)

data class LessonSubmissionResult(
    val completed: Boolean,
    val currentLessonId: String,
    val nextLessonId: String?,
    val todayCompleted: Boolean,
    val missingRequirements: List<String>,
    val ruleChecks: WritingRuleChecks,
    val feedback: WritingReview,
)

data class ProgressSummary(
    val currentLevel: String,
    val currentLessonId: String?,
    val completedCount: Int,
    val nextLessonId: String?,
    val todayCompleted: Boolean,
    val recentWeaknesses: List<String>,
)
