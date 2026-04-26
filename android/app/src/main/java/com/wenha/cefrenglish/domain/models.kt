package com.wenha.cefrenglish.domain

data class PlacementResult(val level: String, val weaknesses: List<String>)

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

data class ProgressSummary(
    val currentLevel: String,
    val completedLessons: List<String>,
    val nextLessonId: String?,
)
