package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.ProgressRepository
import com.wenha.cefrenglish.domain.ProgressSummary

class FakeProgressRepository : ProgressRepository {
    override suspend fun fetchSummary(userId: String): ProgressSummary {
        return ProgressSummary(
            currentLevel = "A1",
            currentLessonId = "A1-01",
            completedCount = 1,
            totalLessonCount = 2,
            progressRatio = 0.5f,
            nextLessonId = "A1-02",
            todayCompleted = false,
            recentWeaknesses = listOf("grammar"),
            reviewItems = emptyList(),
        )
    }

    override suspend fun completeReview(userId: String, reviewId: String) = Unit
}
