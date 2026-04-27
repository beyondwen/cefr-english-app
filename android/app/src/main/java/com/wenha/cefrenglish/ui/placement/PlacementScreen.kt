package com.wenha.cefrenglish.ui.placement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PlacementScreen(
    viewModel: PlacementViewModel,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var readingCorrect by remember { mutableStateOf("4") }
    var grammarCorrect by remember { mutableStateOf("2") }
    var writingWordCount by remember { mutableStateOf("70") }
    val readingValue = readingCorrect.toIntOrNull()
    val grammarValue = grammarCorrect.toIntOrNull()
    val writingValue = writingWordCount.toIntOrNull()
    val canSubmit = readingValue != null && grammarValue != null && writingValue != null

    Column(modifier = Modifier.padding(16.dp)) {
        Text("CEFR Placement")
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Enter your current placement result inputs to set the starting level once.",
        )
        Text("Level: ${state.level.ifBlank { "not assessed" }}")
        Text("Weaknesses: ${state.weaknesses.joinToString().ifBlank { "none" }}")
        OutlinedTextField(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            value = readingCorrect,
            onValueChange = { readingCorrect = it.filter(Char::isDigit) },
            label = { Text("Reading correct") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        OutlinedTextField(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            value = grammarCorrect,
            onValueChange = { grammarCorrect = it.filter(Char::isDigit) },
            label = { Text("Grammar correct") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        OutlinedTextField(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            value = writingWordCount,
            onValueChange = { writingWordCount = it.filter(Char::isDigit) },
            label = { Text("Writing word count") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        Button(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            enabled = canSubmit,
            onClick = {
                viewModel.submitAssessment(
                    readingCorrect = readingValue ?: 0,
                    grammarCorrect = grammarValue ?: 0,
                    writingWordCount = writingValue ?: 0,
                )
            },
        ) {
            Text("Run placement")
        }
        Button(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            enabled = state.level.isNotBlank(),
            onClick = onContinue,
        ) {
            Text("Continue to today")
        }
    }
}
