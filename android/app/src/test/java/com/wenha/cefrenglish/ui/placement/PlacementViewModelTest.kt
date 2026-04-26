package com.wenha.cefrenglish.ui.placement

import com.wenha.cefrenglish.testdoubles.FakePlacementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

class PlacementViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun submitAssessment_updatesLevel() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakePlacementRepository(level = "A2", weaknesses = listOf("grammar"))
            val viewModel = PlacementViewModel(repository)

            viewModel.submitAssessment(readingCorrect = 4, grammarCorrect = 2, writingWordCount = 70)

            assertEquals("A2", viewModel.uiState.value.level)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
