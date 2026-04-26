package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.ProgressRepository
import com.wenha.cefrenglish.domain.ProgressSummary

class FakeProgressRepository : ProgressRepository {
    override suspend fun fetchSummary(userId: String): ProgressSummary {
        return ProgressSummary(currentLevel = "A1", completedLessons = listOf("A1-01"), nextLessonId = "A1-02")
    }
}
