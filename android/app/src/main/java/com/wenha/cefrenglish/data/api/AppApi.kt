package com.wenha.cefrenglish.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AppApi {
    @POST("api/placement/assess")
    suspend fun assessPlacement(@Body request: PlacementRequestDto): PlacementResultDto

    @GET("api/today-lesson")
    suspend fun getTodayLesson(@Query("userId") userId: String): DailyLessonDto

    @POST("api/today-lesson/regenerate")
    suspend fun regenerateTodayLesson(@Body request: TodayLessonRegenerateRequestDto): DailyLessonDto

    @POST("api/lessons/next")
    suspend fun getNextLesson(@Body request: NextLessonRequestDto): LessonDto

    @POST("api/lesson/submit")
    suspend fun submitLesson(@Body request: LessonSubmitRequestDto): LessonSubmitResultDto

    @POST("api/writing/review")
    suspend fun submitWriting(@Body request: WritingReviewRequestDto): WritingReviewResultDto

    @POST("api/progress/complete")
    suspend fun completeLesson(@Body request: CompleteLessonRequestDto): CompleteLessonResultDto

    @GET("api/me/summary")
    suspend fun fetchSummary(@Query("userId") userId: String): ProgressSummaryDto
}
