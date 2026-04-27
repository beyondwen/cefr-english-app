package com.wenha.cefrenglish.ui.lesson

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LessonScreen(
    viewModel: LessonViewModel,
    userId: String,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadTodayLesson(userId)
    }
    LaunchedEffect(state.completed) {
        if (state.completed) onContinue()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Lesson ${state.templateId.ifBlank { "loading" }}")
        Text(state.readingText, modifier = Modifier.padding(top = 8.dp))
        state.readingQuestions.forEachIndexed { index, question ->
            OutlinedTextField(
                value = state.readingAnswers.getOrElse(index) { "" },
                onValueChange = { viewModel.updateReadingAnswer(index, it) },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                label = { Text(question.prompt) },
            )
        }

        Text(state.grammarExplanation, modifier = Modifier.padding(top = 12.dp))
        state.grammarQuestions.forEachIndexed { index, question ->
            OutlinedTextField(
                value = state.grammarAnswers.getOrElse(index) { "" },
                onValueChange = { viewModel.updateGrammarAnswer(index, it) },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                label = { Text(question.prompt) },
            )
        }

        Text(state.writingPrompt, modifier = Modifier.padding(top = 12.dp))
        OutlinedTextField(
            value = state.writingSubmission,
            onValueChange = { viewModel.updateWriting(it) },
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            label = { Text("Your writing") },
        )
        state.writingRubric.forEach {
            Text(it, modifier = Modifier.padding(top = 4.dp))
        }

        Button(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            onClick = { viewModel.regenerate(userId) },
        ) {
            Text("Regenerate lesson")
        }

        Button(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            enabled = state.lessonInstanceId.isNotBlank(),
            onClick = { viewModel.submitLesson(userId) },
        ) {
            Text("Submit lesson")
        }

        state.submissionResult?.missingRequirements?.forEach {
            Text("Missing: $it", modifier = Modifier.padding(top = 4.dp))
        }
        state.feedback?.let {
            Text("Grammar: ${it.grammar}", modifier = Modifier.padding(top = 12.dp))
        }
    }
}
