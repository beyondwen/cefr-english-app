package com.wenha.cefrenglish.ui.home

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
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartPlacement: () -> Unit,
    onContinueLesson: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Today")
        Text("Level: ${state.currentLevel.ifBlank { "A1" }}")
        Text("Current lesson: ${state.currentLessonId ?: "none"}")
        Text("Completed: ${state.completedCount}")
        Text("Weaknesses: ${state.recentWeaknesses.joinToString().ifBlank { "none" }}")

        if (state.shouldStartPlacement) {
            Button(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                onClick = onStartPlacement,
            ) {
                Text("Start placement")
            }
        } else {
            Button(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                onClick = onContinueLesson,
            ) {
                Text(if (state.todayCompleted) "Review progress" else "Continue lesson")
            }
        }
    }
}
