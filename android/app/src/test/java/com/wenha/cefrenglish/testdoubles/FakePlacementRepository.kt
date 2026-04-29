package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.PlacementRepository
import com.wenha.cefrenglish.domain.PlacementQuestion
import com.wenha.cefrenglish.domain.PlacementResult
import com.wenha.cefrenglish.domain.PlacementTest

class FakePlacementRepository(
    private val level: String,
    private val weaknesses: List<String>,
) : PlacementRepository {
    override suspend fun fetchTest(): PlacementTest {
        val question = PlacementQuestion("Choose the right answer.", listOf("a", "b", "c"), 0)
        return PlacementTest(
            readingPassage = "Sample passage",
            readingQuestions = List(5) { question },
            grammarQuestions = List(5) { question },
            writingPrompt = "Write about today.",
            minWritingWords = 30,
        )
    }

    override suspend fun assess(readingCorrect: Int, grammarCorrect: Int, writingWordCount: Int): PlacementResult {
        return PlacementResult(level = level, weaknesses = weaknesses)
    }
}
