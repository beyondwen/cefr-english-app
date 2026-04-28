package com.wenha.cefrenglish.ui.progress

import com.wenha.cefrenglish.testdoubles.FakeProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun refresh_setsCompletedCountAndNextLesson() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeProgressRepository()
            val viewModel = ProgressViewModel(repository)

            viewModel.refresh("u1")

            assertEquals(1, viewModel.uiState.value.completedCount)
            assertEquals("A1-02", viewModel.uiState.value.nextLessonId)
            assertEquals(0.5f, viewModel.uiState.value.progressRatio)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
