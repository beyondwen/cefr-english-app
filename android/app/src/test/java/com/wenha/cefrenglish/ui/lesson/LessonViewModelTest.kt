package com.wenha.cefrenglish.ui.lesson

import com.wenha.cefrenglish.testdoubles.FakeLessonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LessonViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun submitLesson_requiresAtLeastTwoSentences() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeLessonRepository()
            val viewModel = LessonViewModel(repository)

            viewModel.loadTodayLesson("u1")
            viewModel.updateWriting("I like coffee.")
            viewModel.submitLesson("u1")

            assertEquals(false, viewModel.uiState.value.completed)
            assertEquals(listOf("writing_min_sentences"), viewModel.uiState.value.submissionResult?.missingRequirements)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun regenerateLesson_replacesLessonVersion() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeLessonRepository()
            val viewModel = LessonViewModel(repository)

            viewModel.loadTodayLesson("u1")
            val firstId = viewModel.uiState.value.lessonInstanceId
            viewModel.regenerate("u1")

            assertNotEquals(firstId, viewModel.uiState.value.lessonInstanceId)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
