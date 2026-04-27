package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.ProgressRepository
import com.wenha.cefrenglish.domain.ProgressSummary

class FakeHomeRepository(
    private val summary: ProgressSummary = ProgressSummary(
        currentLevel = "A2",
        currentLessonId = "A2-01",
        completedCount = 3,
        nextLessonId = "A2-02",
        todayCompleted = false,
        recentWeaknesses = listOf("grammar"),
    ),
) : ProgressRepository {
    override suspend fun fetchSummary(userId: String): ProgressSummary = summary
}
