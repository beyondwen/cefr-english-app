package com.wenha.cefrenglish.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.wenha.cefrenglish.domain.ReviewItem

val AppBlue = Color(0xFF0057D9)
val AppSky = Color(0xFF17A8FF)
val AppInk = Color(0xFF102033)
val AppMuted = Color(0xFF667085)
val AppLine = Color(0xFFE6ECF5)
val AppBg = Color(0xFFF5F8FC)
val AppGreen = Color(0xFF16A34A)
val AppYellow = Color(0xFFFFC857)
val AppRed = Color(0xFFDC2626)
val AppHeroStatusBar = Color(0xFF0047C7)

private val CardShape = RoundedCornerShape(8.dp)
private val PillShape = RoundedCornerShape(999.dp)

@Composable
fun LearningTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = AppBlue,
            secondary = AppSky,
            background = AppBg,
            surface = Color.White,
            onPrimary = Color.White,
            onSurface = AppInk,
        ),
        content = content,
    )
}

@Composable
fun LearningPage(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBg,
        content = content,
    )
}

@Composable
fun SyncStatusBarWithHero(scrollState: ScrollState) {
    val switchOffsetPx = with(LocalDensity.current) { 80.dp.roundToPx() }
    val overHero = scrollState.value < switchOffsetPx
    StatusBarColors(
        color = if (overHero) AppHeroStatusBar else AppBg,
        darkIcons = !overHero,
    )
}

@Composable
private fun StatusBarColors(color: Color, darkIcons: Boolean) {
    val view = LocalView.current
    val window = view.context.findActivity()?.window
    if (window != null && !view.isInEditMode) {
        SideEffect {
            window.statusBarColor = color.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun HeroPanel(
    title: String,
    subtitle: String,
    badge: String,
    modifier: Modifier = Modifier,
    badgeContent: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    footerContent: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0047C7), Color(0xFF0E8DFF)),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 28.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (badgeContent != null) {
                badgeContent()
            } else {
                Pill(text = badge, color = Color.White.copy(alpha = 0.16f), contentColor = Color.White)
            }
            trailing?.invoke()
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = subtitle,
            color = Color.White.copy(alpha = 0.86f),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (footerContent != null) {
            Spacer(Modifier.height(14.dp))
            footerContent()
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AppLine),
        content = {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        },
    )
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = AppInk, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Text(subtitle, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ErrorNotice(message: String, onRetry: (() -> Unit)? = null) {
    SectionCard {
        SectionTitle("加载失败", message)
        if (onRetry != null) {
            SecondaryAction(text = "重试", onClick = onRetry)
        }
    }
}

@Composable
fun LoadingNotice(message: String) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = AppBlue)
            Text(message, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun Pill(
    text: String,
    color: Color = Color(0xFFEAF3FF),
    contentColor: Color = AppBlue,
) {
    Text(
        modifier = Modifier
            .clip(PillShape)
            .background(color)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        text = text,
        color = contentColor,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun ChipRow(items: List<String>, emptyText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (items.isEmpty()) {
            Pill(emptyText, color = Color(0xFFF2F5F9), contentColor = AppMuted)
        } else {
            items.forEach { Pill(it) }
        }
    }
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(Color(0xFFF7FAFE))
            .border(1.dp, AppLine, CardShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = AppMuted, style = MaterialTheme.typography.labelMedium)
        Text(value, color = AppInk, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LessonProgress(progress: Float, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
            Text("${(progress.coerceIn(0f, 1f) * 100).toInt()}%", color = AppBlue, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(PillShape),
            progress = { progress.coerceIn(0f, 1f) },
            color = AppGreen,
            trackColor = Color(0xFFE4EAF2),
        )
    }
}

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        onClick = onClick,
        shape = CardShape,
        colors = ButtonDefaults.buttonColors(containerColor = AppBlue, disabledContainerColor = Color(0xFFB7C5DA)),
        contentPadding = PaddingValues(horizontal = 18.dp),
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        onClick = onClick,
        shape = CardShape,
        border = BorderStroke(1.dp, AppLine),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppBlue),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StepBadge(text: String, active: Boolean = true) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (active) AppBlue else Color(0xFFE8EEF6)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (active) Color.White else AppMuted, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReviewTaskList(
    items: List<ReviewItem>,
    completingReviewId: String? = null,
    onComplete: (String) -> Unit,
) {
    if (items.isEmpty()) {
        Text("暂无复习任务", color = AppMuted)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { item ->
                val completing = item.reviewId == completingReviewId
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        color = AppInk,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = buildReviewMeta(item),
                        color = AppBlue,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(item.task, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
                    item.steps.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", color = AppMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    SecondaryAction(
                        text = when {
                            completing -> "更新中..."
                            item.status == "completed" -> "再次完成"
                            else -> "标记完成"
                        },
                        enabled = !completing,
                        onClick = { onComplete(item.reviewId) },
                    )
                }
            }
        }
    }
}

private fun buildReviewMeta(item: ReviewItem): String {
    val status = if (item.status == "completed") "已完成" else "待复习"
    val due = item.dueDate?.let { " · 到期 $it" } ?: ""
    return "$status$due · 掌握度 ${item.masteryScore}/3"
}
