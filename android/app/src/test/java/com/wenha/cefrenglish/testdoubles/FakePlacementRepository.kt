package com.wenha.cefrenglish.testdoubles

import com.wenha.cefrenglish.data.PlacementRepository
import com.wenha.cefrenglish.domain.PlacementResult

class FakePlacementRepository(
    private val level: String,
    private val weaknesses: List<String>,
) : PlacementRepository {
    override suspend fun assess(readingCorrect: Int, grammarCorrect: Int, writingWordCount: Int): PlacementResult {
        return PlacementResult(level = level, weaknesses = weaknesses)
    }
}
