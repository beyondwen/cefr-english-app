package com.wenha.cefrenglish.data

import com.wenha.cefrenglish.data.api.AppApi
import com.wenha.cefrenglish.data.api.PlacementAnswerDto
import com.wenha.cefrenglish.data.api.PlacementQuestionDto
import com.wenha.cefrenglish.data.api.PlacementRequestDto
import com.wenha.cefrenglish.data.api.PlacementTestDto
import com.wenha.cefrenglish.data.store.UserPrefsStore
import com.wenha.cefrenglish.domain.PlacementQuestion
import com.wenha.cefrenglish.domain.PlacementResult
import com.wenha.cefrenglish.domain.PlacementTest

interface PlacementRepository {
    suspend fun fetchTest(): PlacementTest
    suspend fun assess(readingCorrect: Int, grammarCorrect: Int, writingWordCount: Int): PlacementResult
}

class NetworkPlacementRepository(
    private val api: AppApi,
    private val userPrefsStore: UserPrefsStore,
) : PlacementRepository {
    override suspend fun fetchTest(): PlacementTest = api.fetchPlacementTest().toDomain()

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

private fun PlacementTestDto.toDomain(): PlacementTest =
    PlacementTest(
        readingPassage = readingPassage,
        readingQuestions = readingQuestions.map { it.toDomain() },
        grammarQuestions = grammarQuestions.map { it.toDomain() },
        writingPrompt = writingPrompt,
        minWritingWords = minWritingWords,
    )

private fun PlacementQuestionDto.toDomain(): PlacementQuestion =
    PlacementQuestion(
        prompt = prompt,
        options = options,
        correctIndex = correctIndex,
    )
