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
            currentLessonId = response.currentLessonId,
            completedCount = response.completedCount,
            totalLessonCount = response.totalLessonCount,
            progressRatio = response.progressRatio,
            nextLessonId = response.nextLessonId,
            todayCompleted = response.todayCompleted,
            recentWeaknesses = response.recentWeaknesses,
        )
    }
}
