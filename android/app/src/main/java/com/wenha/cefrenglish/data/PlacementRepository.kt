package com.wenha.cefrenglish.data

import com.wenha.cefrenglish.data.api.AppApi
import com.wenha.cefrenglish.data.api.PlacementAnswerDto
import com.wenha.cefrenglish.data.api.PlacementRequestDto
import com.wenha.cefrenglish.data.store.UserPrefsStore
import com.wenha.cefrenglish.domain.PlacementResult

interface PlacementRepository {
    suspend fun assess(readingCorrect: Int, grammarCorrect: Int, writingWordCount: Int): PlacementResult
}

class NetworkPlacementRepository(
    private val api: AppApi,
    private val userPrefsStore: UserPrefsStore,
) : PlacementRepository {
    override suspend fun assess(readingCorrect: Int, grammarCorrect: Int, writingWordCount: Int): PlacementResult {
        val userId = userPrefsStore.getOrCreateUserId()
        val response = api.assessPlacement(
            PlacementRequestDto(
                userId = userId,
                answers = listOf(
                    PlacementAnswerDto(skill = "reading", correct = readingCorrect, total = 5),
                    PlacementAnswerDto(skill = "grammar", correct = grammarCorrect, total = 5),
                ),
                writingWordCount = writingWordCount,
            ),
        )
        return PlacementResult(level = response.level, weaknesses = response.weaknesses)
    }
}
