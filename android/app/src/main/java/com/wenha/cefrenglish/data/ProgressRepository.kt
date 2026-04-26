package com.wenha.cefrenglish.data

import com.wenha.cefrenglish.data.api.AppApi
import com.wenha.cefrenglish.domain.ProgressSummary

interface ProgressRepository {
    suspend fun fetchSummary(userId: String): ProgressSummary
}

class NetworkProgressRepository(private val api: AppApi) : ProgressRepository {
    override suspend fun fetchSummary(userId: String): ProgressSummary {
        val response = api.fetchSummary(userId)
        return ProgressSummary(
            currentLevel = response.currentLevel,
            completedLessons = List(response.completedCount) { index -> "completed-$index" },
            nextLessonId = response.nextLessonId,
        )
    }
}
