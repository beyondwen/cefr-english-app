package com.wenha.cefrenglish.ui.home

import com.wenha.cefrenglish.testdoubles.FakeHomeRepository
import com.wenha.cefrenglish.testdoubles.FakeSyllabusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadSummary_setsCurrentLessonAndWeaknesses() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeHomeRepository()
            val viewModel = HomeViewModel(repository, FakeSyllabusRepository())

            viewModel.refresh("u1")

            assertEquals("A2-01", viewModel.uiState.value.currentLessonId)
            assertEquals(listOf("grammar"), viewModel.uiState.value.recentWeaknesses)
            assertEquals(0.5f, viewModel.uiState.value.progressRatio)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun regenerateSyllabus_whenRequestFails_showsErrorAndStopsLoading() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val viewModel = HomeViewModel(
                FakeHomeRepository(),
                FakeSyllabusRepository(regenerateError = IllegalStateException("Unauthorized")),
            )

            viewModel.regenerateSyllabus("A1")

            assertNull(viewModel.uiState.value.regeneratingLevel)
            assertEquals("无法重新生成 A1 课程大纲，请检查网络或稍后重试。", viewModel.uiState.value.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun completeReview_marksReviewAndRefreshesSummary() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeHomeRepository()
            val viewModel = HomeViewModel(repository, FakeSyllabusRepository())

            viewModel.refresh("u1")
            viewModel.completeReview("weakness:grammar")

            assertEquals("weakness:grammar", repository.completedReviewId)
            assertNull(viewModel.uiState.value.completingReviewId)
            assertNull(viewModel.uiState.value.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
