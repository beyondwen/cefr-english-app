package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.LessonRepository
import com.wenha.cefrenglish.domain.DailyLesson
import com.wenha.cefrenglish.domain.LessonAnswerFeedback
import com.wenha.cefrenglish.domain.LessonAnswerFeedbackGroup
import com.wenha.cefrenglish.domain.LessonQuestion
import com.wenha.cefrenglish.domain.LessonSubmissionResult
import com.wenha.cefrenglish.domain.WritingReview
import com.wenha.cefrenglish.domain.WritingRuleChecks

class FakeLessonRepository : LessonRepository {
    private var version = 1
    var lastWritingRevision: String? = null
        private set

    override suspend fun getTodayLesson(userId: String): DailyLesson {
        return DailyLesson(
            lessonInstanceId = "u1:A1-01:v$version",
            templateId = "A1-01",
            level = "A1",
            theme = "daily life",
            readingText = "Sample reading",
            readingQuestions = listOf(
                LessonQuestion("r1", "Reading question 1", emptyList(), "answer"),
                LessonQuestion("r2", "Reading question 2", emptyList(), "answer"),
                LessonQuestion("r3", "Reading question 3", emptyList(), "answer"),
            ),
            grammarExplanation = "Sample grammar",
            grammarQuestions = listOf(
                LessonQuestion("g1", "Grammar question 1", emptyList(), "answer"),
                LessonQuestion("g2", "Grammar question 2", emptyList(), "answer"),
                LessonQuestion("g3", "Grammar question 3", emptyList(), "answer"),
            ),
            writingPrompt = "Write about your weekly routine.",
            writingRubric = listOf("Use 2-4 sentences."),
            generationVersion = version,
        )
    }

    override suspend fun getSyllabusLesson(userId: String, level: String, moduleIndex: Int): DailyLesson {
        return getTodayLesson(userId).copy(
            lessonInstanceId = "$userId:$level-module-${moduleIndex + 1}:v$version",
            level = level,
            theme = "module ${moduleIndex + 1}",
        )
    }

    override suspend fun regenerateTodayLesson(userId: String): DailyLesson {
        version += 1
        return getTodayLesson(userId)
    }

    override suspend fun submitLesson(
        userId: String,
        lessonInstanceId: String,
        prompt: String,
        level: String,
        readingAnswers: List<String>,
        grammarAnswers: List<String>,
        writingSubmission: String,
        writingRevision: String?,
    ): LessonSubmissionResult {
        lastWritingRevision = writingRevision
        val sentenceCount = writingSubmission
            .split(".", "!", "?")
            .map { it.trim() }
            .count { it.isNotBlank() }
        val readingFeedback = listOfNotNull(
            if (readingAnswers.firstOrNull() == "wrong") {
                LessonAnswerFeedback("r1", "Reading question 1", "answer", "wrong", false)
            } else {
                null
            },
        )
        val grammarFeedback = listOfNotNull(
            if (grammarAnswers.getOrNull(1) == "wrong") {
                LessonAnswerFeedback("g2", "Grammar question 2", "answer", "wrong", false)
            } else {
                null
            },
        )
        val completed = readingAnswers.size >= 3 && grammarAnswers.size >= 3 && sentenceCount >= 2 &&
            readingFeedback.isEmpty() &&
            grammarFeedback.isEmpty()
        return LessonSubmissionResult(
            completed = completed,
            currentLessonId = "A1-01",
            nextLessonId = if (completed) "A1-02" else "A1-01",
            todayCompleted = completed,
            missingRequirements = if (completed) {
                emptyList()
            } else if (readingFeedback.isNotEmpty() || grammarFeedback.isNotEmpty()) {
                listOf("reading_incorrect", "grammar_incorrect")
            } else {
                listOf("writing_min_sentences")
            },
            ruleChecks = WritingRuleChecks(
                notBlank = writingSubmission.isNotBlank(),
                minSentencesOk = sentenceCount >= 2,
                onTopicLikely = writingSubmission.length >= 15,
            ),
            feedback = WritingReview("g", "v", "c", emptyList(), "rewrite"),
            revisionRequired = false,
            answerFeedback = LessonAnswerFeedbackGroup(readingFeedback, grammarFeedback),
        )
    }
}
