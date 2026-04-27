package com.wenha.cefrenglish.ui.home

import com.wenha.cefrenglish.testdoubles.FakeHomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadSummary_setsCurrentLessonAndWeaknesses() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeHomeRepository()
            val viewModel = HomeViewModel(repository)

            viewModel.refresh("u1")

            assertEquals("A2-01", viewModel.uiState.value.currentLessonId)
            assertEquals(listOf("grammar"), viewModel.uiState.value.recentWeaknesses)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
