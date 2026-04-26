package com.wenha.cefrenglish.data

import com.wenha.cefrenglish.data.api.AppApi
import com.wenha.cefrenglish.data.api.CompleteLessonRequestDto
import com.wenha.cefrenglish.data.api.NextLessonRequestDto
import com.wenha.cefrenglish.data.api.WritingReviewRequestDto
import com.wenha.cefrenglish.domain.Lesson
import com.wenha.cefrenglish.domain.WritingReview

interface LessonRepository {
    suspend fun getNextLesson(userId: String, level: String): Lesson
    suspend fun submitWriting(userId: String, lessonId: String, level: String, prompt: String, submission: String): WritingReview
    suspend fun completeLesson(userId: String, level: String, lessonId: String)
}

class NetworkLessonRepository(private val api: AppApi) : LessonRepository {
    override suspend fun getNextLesson(userId: String, level: String): Lesson {
        val response = api.getNextLesson(NextLessonRequestDto(userId = userId, level = level))
        return Lesson(
            lessonId = response.lessonId,
            level = response.level,
            readingText = response.readingText,
            grammarExplanation = response.grammarExplanation,
            writingPrompt = response.writingPrompt,
        )
    }

    override suspend fun submitWriting(
        userId: String,
        lessonId: String,
        level: String,
        prompt: String,
        submission: String,
    ): WritingReview {
        val response = api.submitWriting(
            WritingReviewRequestDto(
                userId = userId,
                lessonId = lessonId,
                level = level,
                prompt = prompt,
                submission = submission,
            ),
        )
        return WritingReview(
            grammar = response.feedback.grammar,
            vocabulary = response.feedback.vocabulary,
            coherence = response.feedback.coherence,
            suggestions = response.feedback.suggestions,
            rewrite = response.feedback.rewrite,
        )
    }

    override suspend fun completeLesson(userId: String, level: String, lessonId: String) {
        api.completeLesson(CompleteLessonRequestDto(userId = userId, level = level, lessonId = lessonId))
    }
}
