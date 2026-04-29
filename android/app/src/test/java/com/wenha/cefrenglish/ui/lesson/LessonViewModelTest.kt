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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun submitLesson_keepsAnswerFeedbackForCorrection() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeLessonRepository()
            val viewModel = LessonViewModel(repository)

            viewModel.loadTodayLesson("u1")
            viewModel.updateReadingAnswer(0, "wrong")
            viewModel.updateReadingAnswer(1, "answer")
            viewModel.updateReadingAnswer(2, "answer")
            viewModel.updateGrammarAnswer(0, "answer")
            viewModel.updateGrammarAnswer(1, "wrong")
            viewModel.updateGrammarAnswer(2, "answer")
            viewModel.updateWriting("I study English today. I write two sentences.")
            viewModel.submitLesson("u1")

            val feedback = viewModel.uiState.value.submissionResult?.answerFeedback
            assertEquals(false, viewModel.uiState.value.completed)
            assertEquals("Reading question 1", feedback?.reading?.first()?.prompt)
            assertEquals("Grammar question 2", feedback?.grammar?.first()?.prompt)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun submitLesson_omitsWritingRevisionBeforeRevisionIsRequired() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeLessonRepository()
            val viewModel = LessonViewModel(repository)

            viewModel.loadTodayLesson("u1")
            viewModel.updateReadingAnswer(0, "answer")
            viewModel.updateReadingAnswer(1, "answer")
            viewModel.updateReadingAnswer(2, "answer")
            viewModel.updateGrammarAnswer(0, "answer")
            viewModel.updateGrammarAnswer(1, "answer")
            viewModel.updateGrammarAnswer(2, "answer")
            viewModel.updateWriting("I study English today. I write two sentences.")
            viewModel.submitLesson("u1")

            assertEquals(null, repository.lastWritingRevision)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
