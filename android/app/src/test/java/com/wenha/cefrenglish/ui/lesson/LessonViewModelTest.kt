package com.wenha.cefrenglish.ui.lesson

import com.wenha.cefrenglish.testdoubles.FakeLessonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

class LessonViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadLesson_setsWritingPrompt() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeLessonRepository()
            val viewModel = LessonViewModel(repository)

            viewModel.loadLesson(userId = "u1", level = "A1")

            assertEquals("A1-01", viewModel.uiState.value.lessonId)
            assertEquals("Write about your weekly routine.", viewModel.uiState.value.writingPrompt)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
