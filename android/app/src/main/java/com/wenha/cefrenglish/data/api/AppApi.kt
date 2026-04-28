package com.wenha.cefrenglish.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AppApi {
    @GET("api/placement/test")
    suspend fun fetchPlacementTest(): PlacementTestDto

    @POST("api/placement/assess")
    suspend fun assessPlacement(@Body request: PlacementRequestDto): PlacementResultDto

    @GET("api/syllabus")
    suspend fun fetchSyllabus(@Query("level") level: String): CourseSyllabusDto

    @POST("api/syllabus/regenerate")
    suspend fun regenerateSyllabus(@Body request: RegenerateSyllabusRequestDto): CourseSyllabusDto

    @GET("api/today-lesson")
    suspend fun getTodayLesson(@Query("userId") userId: String): DailyLessonDto

    @GET("api/syllabus-lesson")
    suspend fun getSyllabusLesson(
        @Query("userId") userId: String,
        @Query("level") level: String,
        @Query("moduleIndex") moduleIndex: Int,
    ): DailyLessonDto

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
