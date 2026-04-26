package com.wenha.cefrenglish.ui.placement

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
fun PlacementScreen(
    viewModel: PlacementViewModel,
    onContinue: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("CEFR Placement")
        Text("Level: ${state.level.ifBlank { "not assessed" }}")
        Text("Weaknesses: ${state.weaknesses.joinToString().ifBlank { "none" }}")
        Button(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            onClick = { viewModel.submitAssessment(readingCorrect = 4, grammarCorrect = 2, writingWordCount = 70) },
        ) {
            Text("Run placement")
        }
        Button(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            enabled = state.level.isNotBlank(),
            onClick = { onContinue(state.level) },
        ) {
            Text("Continue to lesson")
        }
    }
}
