package com.wenha.cefrenglish.ui.lesson

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LessonScreen(
    viewModel: LessonViewModel,
    userId: String,
    level: String,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var submission by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Lesson ${state.lessonId.ifBlank { "loading" }}")
        Text(state.readingText)
        Text(state.grammarExplanation)
        Text(state.writingPrompt)
        OutlinedTextField(
            value = submission,
            onValueChange = { submission = it },
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            label = { Text("Your writing") },
        )
        Button(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            onClick = { viewModel.loadLesson(userId, level) },
        ) {
            Text("Load lesson")
        }
        Button(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            enabled = state.lessonId.isNotBlank(),
            onClick = {
                viewModel.submitWriting(
                    userId = userId,
                    level = level,
                    submission = submission,
                )
            },
        ) {
            Text("Submit writing")
        }
        state.feedback?.let {
            Text("Grammar: ${it.grammar}", modifier = Modifier.padding(top = 12.dp))
        }
        Button(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            enabled = state.lessonId.isNotBlank(),
            onClick = {
                viewModel.completeLesson(userId = userId, level = level)
                onContinue()
            },
        ) {
            Text("Complete lesson")
        }
    }
}
