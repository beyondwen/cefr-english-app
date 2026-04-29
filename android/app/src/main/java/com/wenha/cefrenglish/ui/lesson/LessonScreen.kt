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
import com.wenha.cefrenglish.domain.LessonAnswerFeedback
import com.wenha.cefrenglish.domain.DialogueLine
import com.wenha.cefrenglish.domain.KeySentence
import com.wenha.cefrenglish.domain.LessonQuestion
import com.wenha.cefrenglish.domain.VocabularyItem
import com.wenha.cefrenglish.ui.common.AppBlue
import com.wenha.cefrenglish.ui.common.AppGreen
import com.wenha.cefrenglish.ui.common.AppInk
import com.wenha.cefrenglish.ui.common.AppMuted
import com.wenha.cefrenglish.ui.common.ChipRow
import com.wenha.cefrenglish.ui.common.ErrorNotice
import com.wenha.cefrenglish.ui.common.HeroPanel
import com.wenha.cefrenglish.ui.common.LearningPage
import com.wenha.cefrenglish.ui.common.LoadingNotice
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
    selectedLevel: String?,
    selectedModuleIndex: Int?,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId, selectedLevel, selectedModuleIndex) {
        if (selectedLevel != null && selectedModuleIndex != null) {
            viewModel.loadSyllabusLesson(userId, selectedLevel, selectedModuleIndex)
        } else {
            viewModel.loadTodayLesson(userId)
        }
    }
    LaunchedEffect(state.completed) {
        if (state.completed) onContinue()
    }
    val canSubmit = state.lessonInstanceId.isNotBlank() &&
        state.readingAnswers.isNotEmpty() &&
        state.grammarAnswers.isNotEmpty() &&
        state.readingAnswers.all { it.isNotBlank() } &&
        state.grammarAnswers.all { it.isNotBlank() } &&
        state.writingSubmission.isNotBlank() &&
        (state.submissionResult?.revisionRequired != true || state.writingRevision.isNotBlank()) &&
        !state.isSubmitting &&
        !state.isLoading

    LearningPage {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            HeroPanel(
                title = state.theme.ifBlank { state.templateId.ifBlank { "正在加载课程" } },
                subtitle = state.objectives.firstOrNull()
                    ?: state.readingText.takeIf { it.isNotBlank() }
                    ?: "正在准备词汇、句型、对话、阅读、语法和写作练习。",
                badge = state.level.ifBlank { "今日课程" },
                trailing = { Pill("完整课") },
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LessonSectionHeader()

                if (state.isLoading) {
                    LoadingNotice("正在准备课程内容...")
                }
                state.errorMessage?.let { message ->
                    ErrorNotice(message = message, onRetry = { viewModel.retryLastLoad() })
                }

                InfoListSection(
                    step = "1",
                    title = "学习目标",
                    subtitle = "先明确这节课结束后要会什么。",
                    items = state.objectives,
                    emptyText = "正在加载学习目标...",
                )

                InfoListSection(
                    step = "2",
                    title = "热身问题",
                    subtitle = "用自己的经验进入本课场景。",
                    items = state.warmupQuestions,
                    emptyText = "正在加载热身问题...",
                )

                VocabularySection(state.vocabulary)

                KeySentenceSection(state.keySentences)

                DialogueSection(state.dialogue)

                InfoListSection(
                    step = "6",
                    title = "听力练习",
                    subtitle = "先听关键词和句型，再听对话。",
                    items = state.listeningPractice,
                    emptyText = "正在加载听力练习...",
                )

                InfoListSection(
                    step = "7",
                    title = "开口练习",
                    subtitle = "跟读后替换关键词，说出自己的句子。",
                    items = state.speakingPractice,
                    emptyText = "正在加载开口练习...",
                )

                SectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StepBadge("8")
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
                        StepBadge("9")
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
                        StepBadge("10")
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

                InfoListSection(
                    step = "11",
                    title = "课后复习",
                    subtitle = "完成这些任务后再进入下一节课。",
                    items = state.reviewTasks,
                    emptyText = "正在加载复习任务...",
                )

                state.submissionResult?.missingRequirements?.takeIf { it.isNotEmpty() }?.let { missing ->
                    SectionCard {
                        SectionTitle("提交前检查", "还有几项要求需要补齐。")
                        ChipRow(missing, emptyText = "已准备好")
                    }
                }

                state.submissionResult?.answerFeedback?.let { feedback ->
                    if (feedback.reading.isNotEmpty() || feedback.grammar.isNotEmpty()) {
                        SectionCard {
                            SectionTitle("错题订正", "先订正这些题，再进入下一课。")
                            AnswerFeedbackList("阅读", feedback.reading)
                            AnswerFeedbackList("语法", feedback.grammar)
                        }
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
                        if (it.issues.isNotEmpty()) {
                            it.issues.forEach { issue ->
                                FeedbackLine("写作问题：${issue.errorType}", issue.practicePrompt)
                            }
                        }
                    }
                }

                if (state.submissionResult?.revisionRequired == true) {
                    SectionCard {
                        SectionTitle("订正写作", "根据反馈重写一次，完成订正后再进入下一课。")
                        state.feedback?.rewrite?.takeIf { it.isNotBlank() }?.let { rewrite ->
                            Text("参考改写：$rewrite", color = AppMuted)
                        }
                        OutlinedTextField(
                            value = state.writingRevision,
                            onValueChange = { viewModel.updateWritingRevision(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("你的订正版") },
                            minLines = 5,
                        )
                    }
                }

                if (selectedLevel == null) {
                    SecondaryAction(
                        text = if (state.isLoading) "正在重新生成..." else "重新生成课程",
                        enabled = !state.isLoading && !state.isSubmitting,
                        onClick = { viewModel.regenerate(userId) },
                    )
                }
                PrimaryAction(
                    text = if (state.isSubmitting) "正在提交..." else "提交课程",
                    enabled = canSubmit,
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
        Pill("句型", color = Color(0xFFEFF9F2), contentColor = AppGreen)
        Pill("写作", color = Color(0xFFFFF5D6), contentColor = Color(0xFF946200))
    }
}

@Composable
private fun InfoListSection(step: String, title: String, subtitle: String, items: List<String>, emptyText: String) {
    SectionCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StepBadge(step)
            SectionTitle(title, subtitle)
        }
        if (items.isEmpty()) {
            Text(emptyText, color = AppMuted)
        } else {
            items.forEach { item ->
                BulletText(item)
            }
        }
    }
}

@Composable
private fun VocabularySection(items: List<VocabularyItem>) {
    SectionCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StepBadge("3")
            SectionTitle("核心词汇", "先学会本课最需要的词。")
        }
        if (items.isEmpty()) {
            Text("正在加载核心词汇...", color = AppMuted)
        } else {
            items.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${item.word}  ${item.meaning}", color = AppInk, fontWeight = FontWeight.Bold)
                    if (item.example.isNotBlank()) {
                        Text(item.example, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeySentenceSection(items: List<KeySentence>) {
    SectionCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StepBadge("4")
            SectionTitle("核心句型", "掌握可以替换使用的表达模板。")
        }
        if (items.isEmpty()) {
            Text("正在加载核心句型...", color = AppMuted)
        } else {
            items.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(item.pattern, color = AppBlue, fontWeight = FontWeight.Bold)
                    Text(item.meaning, color = AppMuted)
                    item.examples.forEach { example -> BulletText(example) }
                }
            }
        }
    }
}

@Composable
private fun DialogueSection(items: List<DialogueLine>) {
    SectionCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StepBadge("5")
            SectionTitle("情景对话", "把词汇和句型放进真实交流。")
        }
        if (items.isEmpty()) {
            Text("正在加载情景对话...", color = AppMuted)
        } else {
            items.forEach { item ->
                Text("${item.speaker}: ${item.line}", color = AppInk, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun BulletText(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("•", color = AppGreen, fontWeight = FontWeight.Bold)
        Text(text, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
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
private fun AnswerFeedbackList(label: String, items: List<LessonAnswerFeedback>) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = AppBlue, fontWeight = FontWeight.SemiBold)
        items.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.prompt, color = AppInk, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("你的答案：${item.submittedAnswer.ifBlank { "空白" }}", color = AppMuted, style = MaterialTheme.typography.bodySmall)
                Text("参考答案：${item.expectedAnswer}", color = AppMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FeedbackLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = AppMuted, style = MaterialTheme.typography.labelLarge)
        Text(value, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
