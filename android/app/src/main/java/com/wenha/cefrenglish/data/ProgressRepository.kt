package com.wenha.cefrenglish.data

import com.wenha.cefrenglish.data.api.AppApi
import com.wenha.cefrenglish.data.api.CompleteReviewRequestDto
import com.wenha.cefrenglish.domain.ProgressSummary
import com.wenha.cefrenglish.domain.ReviewItem

interface ProgressRepository {
    suspend fun fetchSummary(userId: String): ProgressSummary
    suspend fun completeReview(userId: String, reviewId: String)
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
            reviewItems = response.reviewItems.map {
                ReviewItem(
                    reviewId = it.reviewId,
                    skill = it.skill,
                    title = it.title,
                    task = it.task,
                    steps = it.steps,
                    status = it.status,
                    masteryScore = it.masteryScore,
                )
            },
        )
    }

    override suspend fun completeReview(userId: String, reviewId: String) {
        api.completeReview(CompleteReviewRequestDto(userId = userId, reviewId = reviewId))
    }
}
