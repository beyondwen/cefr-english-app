package com.wenha.cefrenglish.domain

data class PlacementResult(val level: String, val weaknesses: List<String>)

data class PlacementQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
)

data class PlacementTest(
    val readingPassage: String,
    val readingQuestions: List<PlacementQuestion>,
    val grammarQuestions: List<PlacementQuestion>,
    val writingPrompt: String,
    val minWritingWords: Int,
)

data class LessonQuestion(
    val questionId: String,
    val prompt: String,
    val choices: List<String>,
    val answer: String,
)

data class SyllabusModule(
    val title: String,
    val goal: String,
    val lessons: List<String>,
)

data class CourseSyllabus(
    val level: String,
    val title: String,
    val description: String,
    val modules: List<SyllabusModule>,
)

data class VocabularyItem(
    val word: String,
    val meaning: String,
    val example: String,
)

data class KeySentence(
    val pattern: String,
    val meaning: String,
    val examples: List<String>,
)

data class DialogueLine(
    val speaker: String,
    val line: String,
)

data class DailyLesson(
    val lessonInstanceId: String,
    val templateId: String,
    val level: String,
    val theme: String,
    val objectives: List<String> = emptyList(),
    val warmupQuestions: List<String> = emptyList(),
    val vocabulary: List<VocabularyItem> = emptyList(),
    val keySentences: List<KeySentence> = emptyList(),
    val dialogue: List<DialogueLine> = emptyList(),
    val speakingPractice: List<String> = emptyList(),
    val listeningPractice: List<String> = emptyList(),
    val reviewTasks: List<String> = emptyList(),
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
    val issues: List<WritingIssue> = emptyList(),
)

data class WritingIssue(
    val errorType: String,
    val originalText: String,
    val correction: String,
    val explanation: String,
    val practicePrompt: String,
)

data class WritingRuleChecks(
    val notBlank: Boolean,
    val minSentencesOk: Boolean,
    val onTopicLikely: Boolean,
)

data class LessonAnswerFeedback(
    val questionId: String,
    val prompt: String,
    val expectedAnswer: String,
    val submittedAnswer: String,
    val correct: Boolean,
)

data class LessonAnswerFeedbackGroup(
    val reading: List<LessonAnswerFeedback>,
    val grammar: List<LessonAnswerFeedback>,
)

data class LessonSubmissionResult(
    val completed: Boolean,
    val currentLessonId: String,
    val nextLessonId: String?,
    val todayCompleted: Boolean,
    val missingRequirements: List<String>,
    val ruleChecks: WritingRuleChecks,
    val revisionRequired: Boolean,
    val feedback: WritingReview,
    val answerFeedback: LessonAnswerFeedbackGroup? = null,
)

data class ReviewItem(
    val reviewId: String,
    val skill: String,
    val title: String,
    val task: String,
    val steps: List<String>,
    val dueDate: String?,
    val status: String,
    val masteryScore: Int,
)

data class ProgressSummary(
    val currentLevel: String,
    val currentLessonId: String?,
    val completedCount: Int,
    val totalLessonCount: Int,
    val progressRatio: Float,
    val nextLessonId: String?,
    val todayCompleted: Boolean,
    val recentWeaknesses: List<String>,
    val reviewItems: List<ReviewItem>,
)
