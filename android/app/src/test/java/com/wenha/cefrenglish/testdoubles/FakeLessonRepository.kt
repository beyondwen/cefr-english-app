package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.LessonRepository
import com.wenha.cefrenglish.domain.DailyLesson
import com.wenha.cefrenglish.domain.LessonQuestion
import com.wenha.cefrenglish.domain.LessonSubmissionResult
import com.wenha.cefrenglish.domain.WritingReview
import com.wenha.cefrenglish.domain.WritingRuleChecks

class FakeLessonRepository : LessonRepository {
    private var version = 1

    override suspend fun getTodayLesson(userId: String): DailyLesson {
        return DailyLesson(
            lessonInstanceId = "u1:A1-01:v$version",
            templateId = "A1-01",
            level = "A1",
            theme = "daily life",
            readingText = "Sample reading",
            readingQuestions = listOf(
                LessonQuestion("r1", "Reading question 1", emptyList(), ""),
                LessonQuestion("r2", "Reading question 2", emptyList(), ""),
                LessonQuestion("r3", "Reading question 3", emptyList(), ""),
            ),
            grammarExplanation = "Sample grammar",
            grammarQuestions = listOf(
                LessonQuestion("g1", "Grammar question 1", emptyList(), ""),
                LessonQuestion("g2", "Grammar question 2", emptyList(), ""),
                LessonQuestion("g3", "Grammar question 3", emptyList(), ""),
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
    ): LessonSubmissionResult {
        val sentenceCount = writingSubmission
            .split(".", "!", "?")
            .map { it.trim() }
            .count { it.isNotBlank() }
        val completed = readingAnswers.size >= 3 && grammarAnswers.size >= 3 && sentenceCount >= 2
        return LessonSubmissionResult(
            completed = completed,
            currentLessonId = "A1-01",
            nextLessonId = if (completed) "A1-02" else "A1-01",
            todayCompleted = completed,
            missingRequirements = if (completed) emptyList() else listOf("writing_min_sentences"),
            ruleChecks = WritingRuleChecks(
                notBlank = writingSubmission.isNotBlank(),
                minSentencesOk = sentenceCount >= 2,
                onTopicLikely = writingSubmission.length >= 15,
            ),
            feedback = WritingReview("g", "v", "c", emptyList(), "rewrite"),
        )
    }
}
