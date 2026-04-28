package com.wenha.cefrenglish.ui.placement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wenha.cefrenglish.ui.common.ChipRow
import com.wenha.cefrenglish.ui.common.ErrorNotice
import com.wenha.cefrenglish.ui.common.HeroPanel
import com.wenha.cefrenglish.ui.common.LearningPage
import com.wenha.cefrenglish.ui.common.LoadingNotice
import com.wenha.cefrenglish.ui.common.PrimaryAction
import com.wenha.cefrenglish.ui.common.SecondaryAction
import com.wenha.cefrenglish.ui.common.SectionCard
import com.wenha.cefrenglish.ui.common.SectionTitle
import com.wenha.cefrenglish.domain.PlacementQuestion

@Composable
fun PlacementScreen(
    viewModel: PlacementViewModel,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadTest()
    }
    val test = state.test
    var readingAnswers by remember(test) { mutableStateOf(List(test?.readingQuestions?.size ?: 0) { -1 }) }
    var grammarAnswers by remember(test) { mutableStateOf(List(test?.grammarQuestions?.size ?: 0) { -1 }) }
    var writingAnswer by remember { mutableStateOf("") }
    val readingCorrect = test?.let { readingAnswers.correctCount(it.readingQuestions) } ?: 0
    val grammarCorrect = test?.let { grammarAnswers.correctCount(it.grammarQuestions) } ?: 0
    val writingWordCount = writingAnswer.wordCount()
    val allQuestionsAnswered = readingAnswers.all { it >= 0 } && grammarAnswers.all { it >= 0 }
    val canSubmit = test != null && allQuestionsAnswered && writingWordCount >= test.minWritingWords && !state.isSubmitting

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
                state.errorMessage?.let { message ->
                    ErrorNotice(message = message, onRetry = if (test == null) viewModel::loadTest else null)
                }

                if (test == null) {
                    if (state.isLoadingTest) {
                        LoadingNotice("正在用 AI 生成定级题...")
                    }
                } else {
                SectionCard {
                    SectionTitle(
                        "能力测评",
                        "完成阅读、语法和短写作，系统会按答题情况判断起点。",
                    )
                    Text(
                        text = test.readingPassage,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    test.readingQuestions.forEachIndexed { index, question ->
                        PlacementQuestionBlock(
                            number = index + 1,
                            question = question,
                            selectedIndex = readingAnswers[index],
                            onSelect = { selected ->
                                readingAnswers = readingAnswers.updated(index, selected)
                            },
                        )
                    }
                }

                SectionCard {
                    SectionTitle("语法选择", "选择最自然、最准确的表达。")
                    test.grammarQuestions.forEachIndexed { index, question ->
                        PlacementQuestionBlock(
                            number = index + 1,
                            question = question,
                            selectedIndex = grammarAnswers[index],
                            onSelect = { selected ->
                                grammarAnswers = grammarAnswers.updated(index, selected)
                            },
                        )
                    }
                }

                SectionCard {
                    SectionTitle("短写作", "用英语写 ${test.minWritingWords} 词以上。写得越完整，定级越可靠。")
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = writingAnswer,
                        onValueChange = { writingAnswer = it },
                        label = { Text(test.writingPrompt) },
                        minLines = 5,
                    )
                    Text("当前词数：$writingWordCount / ${test.minWritingWords}")
                    PrimaryAction(
                        text = if (state.isSubmitting) "正在定级..." else "开始定级",
                        enabled = canSubmit,
                        onClick = {
                            viewModel.submitAssessment(
                                readingCorrect = readingCorrect,
                                grammarCorrect = grammarCorrect,
                                writingWordCount = writingWordCount,
                            )
                        },
                    )
                }
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

@Composable
private fun PlacementQuestionBlock(
    number: Int,
    question: PlacementQuestion,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "$number. ${question.prompt}",
            style = MaterialTheme.typography.titleSmall,
        )
        question.options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(index) }
                    .padding(vertical = 2.dp),
            ) {
                RadioButton(
                    selected = selectedIndex == index,
                    onClick = { onSelect(index) },
                )
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun List<Int>.updated(index: Int, value: Int): List<Int> =
    toMutableList().also { it[index] = value }

private fun List<Int>.correctCount(questions: List<PlacementQuestion>): Int =
    indices.count { index -> this[index] == questions[index].correctIndex }

private fun String.wordCount(): Int =
    trim()
        .split(Regex("\\s+"))
        .count { word -> word.any(Char::isLetter) }
