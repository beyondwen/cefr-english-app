package com.wenha.cefrenglish.ui.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Progress")
        Text("Completed: ${state.completedCount}")
        Text("Next lesson: ${state.nextLessonId.ifBlank { "none" }}")
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
