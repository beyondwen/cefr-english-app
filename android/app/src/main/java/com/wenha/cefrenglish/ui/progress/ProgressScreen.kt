package com.wenha.cefrenglish.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wenha.cefrenglish.domain.ReviewItem
import com.wenha.cefrenglish.ui.common.AppBlue
import com.wenha.cefrenglish.ui.common.AppInk
import com.wenha.cefrenglish.ui.common.AppMuted
import com.wenha.cefrenglish.ui.common.ChipRow
import com.wenha.cefrenglish.ui.common.ErrorNotice
import com.wenha.cefrenglish.ui.common.HeroPanel
import com.wenha.cefrenglish.ui.common.LearningPage
import com.wenha.cefrenglish.ui.common.LessonProgress
import com.wenha.cefrenglish.ui.common.LoadingNotice
import com.wenha.cefrenglish.ui.common.PrimaryAction
import com.wenha.cefrenglish.ui.common.SectionCard
import com.wenha.cefrenglish.ui.common.SectionTitle
import com.wenha.cefrenglish.ui.common.SecondaryAction
import com.wenha.cefrenglish.ui.common.StatTile

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    userId: String,
    onRefresh: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.refresh(userId)
        }
    }
    val progress = state.progressRatio

    LearningPage {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            HeroPanel(
                title = "学习进度",
                subtitle = if (state.todayCompleted) {
                    "今日课程已完成，明天继续保持节奏。"
                } else {
                    "查看你的 CEFR 等级、课程进展和当前薄弱项。"
                },
                badge = state.currentLevel.ifBlank { "未设置" },
            )
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.isLoading) {
                    LoadingNotice("正在加载学习进度...")
                }
                state.errorMessage?.let { message ->
                    ErrorNotice(message = message, onRetry = { viewModel.refresh(userId) })
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "已完成",
                        value = "${state.completedCount}",
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "今日",
                        value = if (state.todayCompleted) "已完成" else "待完成",
                    )
                }

                SectionCard {
                    SectionTitle("课程进展", "你当前在每日学习路径中的位置。")
                    LessonProgress(progress = progress, label = "今日完成度")
                    ProgressRow("当前课程", state.currentLessonId.ifBlank { "无" })
                    ProgressRow("下一课程", state.nextLessonId.ifBlank { "无" })
                }

                SectionCard {
                    SectionTitle("最近薄弱项", "后续课程会基于这些内容调整。")
                    ChipRow(state.recentWeaknesses, emptyText = "暂无薄弱项")
                }

                SectionCard {
                    SectionTitle("复习任务", "这些任务来自最近的薄弱项。")
                    ReviewItemList(
                        items = state.reviewItems,
                        onComplete = { viewModel.completeReview(userId, it) },
                    )
                }

                PrimaryAction(
                    text = if (state.isLoading) "正在刷新..." else "刷新进度",
                    enabled = !state.isLoading,
                    onClick = {
                        viewModel.refresh(userId)
                        onRefresh()
                    },
                )
            }
        }
    }
}

@Composable
private fun ReviewItemList(items: List<ReviewItem>, onComplete: (String) -> Unit) {
    if (items.isEmpty()) {
        Text("暂无复习任务", color = AppMuted)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.title, color = AppInk, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (item.status == "completed") "已完成 · 掌握度 ${item.masteryScore}/3" else "待复习 · 掌握度 ${item.masteryScore}/3",
                        color = AppBlue,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(item.task, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
                    item.steps.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", color = AppMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    SecondaryAction(
                        text = if (item.status == "completed") "再次完成" else "标记完成",
                        onClick = { onComplete(item.reviewId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = AppMuted)
        Text(value, color = AppBlue, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
