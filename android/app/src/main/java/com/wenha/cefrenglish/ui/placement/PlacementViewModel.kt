package com.wenha.cefrenglish.ui.placement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.PlacementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PlacementUiState(val level: String = "", val weaknesses: List<String> = emptyList())

class PlacementViewModel(private val repository: PlacementRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PlacementUiState())
    val uiState: StateFlow<PlacementUiState> = _uiState

    fun submitAssessment(readingCorrect: Int, grammarCorrect: Int, writingWordCount: Int) {
        viewModelScope.launch {
            val result = repository.assess(readingCorrect, grammarCorrect, writingWordCount)
            _uiState.value = PlacementUiState(level = result.level, weaknesses = result.weaknesses)
        }
    }
}
