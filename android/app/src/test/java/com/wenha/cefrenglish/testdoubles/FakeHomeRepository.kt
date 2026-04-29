package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.ProgressRepository
import com.wenha.cefrenglish.domain.ProgressSummary

class FakeHomeRepository(
    private val summary: ProgressSummary = ProgressSummary(
        currentLevel = "A2",
        currentLessonId = "A2-01",
        completedCount = 3,
        totalLessonCount = 6,
        progressRatio = 0.5f,
        nextLessonId = "A2-02",
        todayCompleted = false,
        recentWeaknesses = listOf("grammar"),
        reviewItems = emptyList(),
    ),
) : ProgressRepository {
    var completedReviewId: String? = null
        private set

    override suspend fun fetchSummary(userId: String): ProgressSummary = summary
    override suspend fun completeReview(userId: String, reviewId: String) {
        completedReviewId = reviewId
    }
}
