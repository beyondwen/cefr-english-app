package com.wenha.cefrenglish.ui.placement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import com.wenha.cefrenglish.ui.common.ChipRow
import com.wenha.cefrenglish.ui.common.HeroPanel
import com.wenha.cefrenglish.ui.common.LearningPage
import com.wenha.cefrenglish.ui.common.PrimaryAction
import com.wenha.cefrenglish.ui.common.SecondaryAction
import com.wenha.cefrenglish.ui.common.SectionCard
import com.wenha.cefrenglish.ui.common.SectionTitle
import androidx.compose.foundation.verticalScroll

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

    LearningPage {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        )
        {
            HeroPanel(
                title = "定级测评",
                subtitle = "先确定合适的 CEFR 起点，让每日课程从第一天就贴合你的水平。",
                badge = if (state.level.isBlank()) "新学习者" else "CEFR ${state.level}",
            )
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionCard {
                    SectionTitle("快速评分", "输入三个信号，用于判断初始等级。")
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = readingCorrect,
                        onValueChange = { readingCorrect = it.filter(Char::isDigit) },
                        label = { Text("阅读答对数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = grammarCorrect,
                        onValueChange = { grammarCorrect = it.filter(Char::isDigit) },
                        label = { Text("语法答对数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = writingWordCount,
                        onValueChange = { writingWordCount = it.filter(Char::isDigit) },
                        label = { Text("写作词数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    PrimaryAction(
                        text = "开始定级",
                        enabled = canSubmit,
                        onClick = {
                            viewModel.submitAssessment(
                                readingCorrect = readingValue ?: 0,
                                grammarCorrect = grammarValue ?: 0,
                                writingWordCount = writingValue ?: 0,
                            )
                        },
                    )
                }

                SectionCard {
                    SectionTitle(
                        title = "测评结果",
                        subtitle = if (state.level.isBlank()) "完成定级后即可解锁今日课程。" else "你的学习计划已准备好。",
                    )
                    Text("等级：${state.level.ifBlank { "尚未测评" }}")
                    ChipRow(state.weaknesses, emptyText = "暂无薄弱项")
                    SecondaryAction(
                        text = "进入今日课程",
                        enabled = state.level.isNotBlank(),
                        onClick = onContinue,
                    )
                }
            }
        }
    }
}
