package com.wenha.cefrenglish.ui.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    userId: String,
    onRefresh: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.refresh(userId)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Progress")
        Text("Level: ${state.currentLevel.ifBlank { "not set" }}")
        Text("Current lesson: ${state.currentLessonId.ifBlank { "none" }}")
        Text("Completed: ${state.completedCount}")
        Text("Today done: ${if (state.todayCompleted) "yes" else "no"}")
        Text("Next lesson: ${state.nextLessonId.ifBlank { "none" }}")
        Text("Weaknesses: ${state.recentWeaknesses.joinToString().ifBlank { "none" }}")
        Button(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            onClick = {
                viewModel.refresh(userId)
                onRefresh()
            },
        ) {
            Text("Refresh")
        }
    }
}
