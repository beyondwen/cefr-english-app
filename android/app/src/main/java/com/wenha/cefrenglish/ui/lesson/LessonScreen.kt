package com.wenha.cefrenglish.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wenha.cefrenglish.domain.LessonQuestion
import com.wenha.cefrenglish.ui.common.AppBlue
import com.wenha.cefrenglish.ui.common.AppGreen
import com.wenha.cefrenglish.ui.common.AppMuted
import com.wenha.cefrenglish.ui.common.ChipRow
import com.wenha.cefrenglish.ui.common.HeroPanel
import com.wenha.cefrenglish.ui.common.LearningPage
import com.wenha.cefrenglish.ui.common.Pill
import com.wenha.cefrenglish.ui.common.PrimaryAction
import com.wenha.cefrenglish.ui.common.SecondaryAction
import com.wenha.cefrenglish.ui.common.SectionCard
import com.wenha.cefrenglish.ui.common.SectionTitle
import com.wenha.cefrenglish.ui.common.StepBadge

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

    LearningPage {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            HeroPanel(
                title = state.templateId.ifBlank { "正在加载课程" },
                subtitle = state.readingText.takeIf { it.isNotBlank() }
                    ?: "正在准备今日阅读、语法和写作练习。",
                badge = state.level.ifBlank { "今日课程" },
                trailing = { Pill("3 步") },
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LessonSectionHeader()

                SectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StepBadge("1")
                        SectionTitle("阅读", "阅读短文，并简要回答问题。")
                    }
                    Text(
                        text = state.readingText.ifBlank { "正在加载阅读材料..." },
                        color = AppMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    state.readingQuestions.forEachIndexed { index, question ->
                        QuestionField(
                            question = question,
                            value = state.readingAnswers.getOrElse(index) { "" },
                            onValueChange = { viewModel.updateReadingAnswer(index, it) },
                        )
                    }
                }

                SectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StepBadge("2")
                        SectionTitle("语法", "在真实语境中使用目标句型。")
                    }
                    Text(
                        text = state.grammarExplanation.ifBlank { "正在加载语法说明..." },
                        color = AppMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    state.grammarQuestions.forEachIndexed { index, question ->
                        QuestionField(
                            question = question,
                            value = state.grammarAnswers.getOrElse(index) { "" },
                            onValueChange = { viewModel.updateGrammarAnswer(index, it) },
                        )
                    }
                }

                SectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StepBadge("3")
                        SectionTitle("写作", "用一段简短回答完成练习。")
                    }
                    Text(state.writingPrompt.ifBlank { "正在加载写作题目..." }, color = AppMuted)
                    OutlinedTextField(
                        value = state.writingSubmission,
                        onValueChange = { viewModel.updateWriting(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("你的写作") },
                        minLines = 5,
                    )
                    ChipRow(state.writingRubric, emptyText = "正在加载评分标准")
                }

                state.submissionResult?.missingRequirements?.takeIf { it.isNotEmpty() }?.let { missing ->
                    SectionCard {
                        SectionTitle("提交前检查", "还有几项要求需要补齐。")
                        ChipRow(missing, emptyText = "已准备好")
                    }
                }

                state.feedback?.let {
                    SectionCard {
                        SectionTitle("反馈", "继续前先查看本次练习建议。")
                        FeedbackLine("语法", it.grammar)
                        FeedbackLine("词汇", it.vocabulary)
                        FeedbackLine("连贯性", it.coherence)
                        if (it.suggestions.isNotEmpty()) {
                            ChipRow(it.suggestions, emptyText = "暂无建议")
                        }
                    }
                }

                SecondaryAction(
                    text = "重新生成课程",
                    onClick = { viewModel.regenerate(userId) },
                )
                PrimaryAction(
                    text = "提交课程",
                    enabled = state.lessonInstanceId.isNotBlank(),
                    onClick = { viewModel.submitLesson(userId) },
                )
            }
        }
    }
}

@Composable
private fun LessonSectionHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Pill("阅读", color = Color(0xFFEAF3FF), contentColor = AppBlue)
        Pill("语法", color = Color(0xFFEFF9F2), contentColor = AppGreen)
        Pill("写作", color = Color(0xFFFFF5D6), contentColor = Color(0xFF946200))
    }
}

@Composable
private fun QuestionField(
    question: LessonQuestion,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(question.prompt, color = AppBlue, fontWeight = FontWeight.SemiBold)
        if (question.choices.isNotEmpty()) {
            ChipRow(question.choices, emptyText = "自由作答")
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("答案") },
            singleLine = true,
        )
    }
}

@Composable
private fun FeedbackLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = AppMuted, style = MaterialTheme.typography.labelLarge)
        Text(value, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
