package com.wenha.cefrenglish.ui.placement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenha.cefrenglish.data.PlacementRepository
import com.wenha.cefrenglish.domain.PlacementTest
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class PlacementUiState(
    val test: PlacementTest? = null,
    val isLoadingTest: Boolean = false,
    val level: String = "",
    val weaknesses: List<String> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

class PlacementViewModel(private val repository: PlacementRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PlacementUiState())
    val uiState: StateFlow<PlacementUiState> = _uiState

    fun loadTest() {
        if (_uiState.value.test != null || _uiState.value.isLoadingTest) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingTest = true, errorMessage = null)
            runCatching { repository.fetchTest() }
                .onSuccess { test ->
                    _uiState.value = _uiState.value.copy(test = test, isLoadingTest = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoadingTest = false,
                        errorMessage = "AI 定级题加载失败，已使用本地备用题。",
                    )
                }
        }
    }

    fun submitAssessment(readingCorrect: Int, grammarCorrect: Int, writingWordCount: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            runCatching { repository.assess(readingCorrect, grammarCorrect, writingWordCount) }
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        level = result.level,
                        weaknesses = result.weaknesses,
                        isSubmitting = false,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = it.toPlacementErrorMessage(),
                    )
                }
        }
    }
}

private fun Throwable.toPlacementErrorMessage(): String =
    when (this) {
        is HttpException -> when (code()) {
            401 -> "定级失败：后端需要授权，请更新 App 或后端配置。"
            in 500..599 -> "定级失败：服务暂时不可用，请稍后重试。"
            else -> "定级失败：请求参数或服务响应异常（HTTP ${code()}）。"
        }
        is IOException -> "定级失败：无法连接服务器，请检查网络。"
        else -> "定级失败：${message ?: "请稍后重试。"}"
    }
