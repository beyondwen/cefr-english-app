package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.LessonRepository
import com.wenha.cefrenglish.domain.Lesson
import com.wenha.cefrenglish.domain.WritingReview

class FakeLessonRepository : LessonRepository {
    override suspend fun getNextLesson(userId: String, level: String): Lesson {
        return Lesson(
            lessonId = "A1-01",
            level = level,
            readingText = "Sample reading",
            grammarExplanation = "Sample grammar",
            writingPrompt = "Write about your weekly routine.",
        )
    }

    override suspend fun submitWriting(userId: String, lessonId: String, level: String, prompt: String, submission: String): WritingReview {
        return WritingReview("g", "v", "c", emptyList(), "rewrite")
    }

    override suspend fun completeLesson(userId: String, level: String, lessonId: String) = Unit
}
