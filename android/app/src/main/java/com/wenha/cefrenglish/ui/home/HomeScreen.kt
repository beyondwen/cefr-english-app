package com.wenha.cefrenglish.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wenha.cefrenglish.domain.CourseSyllabus
import com.wenha.cefrenglish.domain.SyllabusModule
import com.wenha.cefrenglish.ui.common.AppBlue
import com.wenha.cefrenglish.ui.common.AppGreen
import com.wenha.cefrenglish.ui.common.AppInk
import com.wenha.cefrenglish.ui.common.AppLine
import com.wenha.cefrenglish.ui.common.AppMuted
import com.wenha.cefrenglish.ui.common.ChipRow
import com.wenha.cefrenglish.ui.common.ErrorNotice
import com.wenha.cefrenglish.ui.common.HeroPanel
import com.wenha.cefrenglish.ui.common.LearningPage
import com.wenha.cefrenglish.ui.common.LessonProgress
import com.wenha.cefrenglish.ui.common.LoadingNotice
import com.wenha.cefrenglish.ui.common.Pill
import com.wenha.cefrenglish.ui.common.PrimaryAction
import com.wenha.cefrenglish.ui.common.SectionCard
import com.wenha.cefrenglish.ui.common.SectionTitle
import com.wenha.cefrenglish.ui.common.StatTile

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartModule: (level: String, moduleIndex: Int) -> Unit,
    onStartPlacement: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLevel = state.currentLevel.ifBlank { "A1" }
    var selectedLevel by remember(currentLevel) { mutableStateOf(currentLevel.takeIf { it in cefrLevels } ?: "A1") }
    val syllabus = state.selectedSyllabus?.takeIf { it.level == selectedLevel }
        ?: fallbackSyllabuses.first { it.level == selectedLevel }
    val progress = state.progressRatio

    LearningPage {
        LaunchedEffect(selectedLevel) {
            viewModel.loadSyllabus(selectedLevel)
        }
        LaunchedEffect(state.shouldStartPlacement) {
            if (state.shouldStartPlacement) onStartPlacement()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            HeroPanel(
                title = "课程大纲",
                subtitle = "选择一个 CEFR 等级，查看对应的阅读、语法、词汇和写作学习路径。",
                badge = "CEFR $selectedLevel",
                badgeContent = {
                    LevelDropdownPill(
                        selectedLevel = selectedLevel,
                        onLevelSelected = { selectedLevel = it },
                    )
                },
                trailing = { Pill("已完成 ${state.completedCount}") },
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.isLoading) {
                    LoadingNotice("正在加载学习进度...")
                }
                state.errorMessage?.let { message ->
                    ErrorNotice(message = message, onRetry = { viewModel.retryRefresh() })
                }

                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SectionTitle(
                                title = syllabus.title,
                                subtitle = syllabus.description,
                            )
                            PrimaryAction(
                                text = if (state.regeneratingLevel == selectedLevel) "正在生成大纲..." else "重新生成大纲",
                                enabled = state.regeneratingLevel == null,
                                onClick = { viewModel.regenerateSyllabus(selectedLevel) },
                            )
                        }
                        Pill(syllabus.level, color = Color(0xFFEAF3FF), contentColor = AppBlue)
                    }
                    LessonProgress(progress = progress, label = "当前学习进度")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "当前等级",
                        value = currentLevel,
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "查看等级",
                        value = selectedLevel,
                    )
                }

                syllabus.modules.forEachIndexed { index, module ->
                    SyllabusModuleCard(
                        index = index + 1,
                        module = module,
                        onStart = { onStartModule(selectedLevel, index) },
                    )
                }

                SectionCard {
                    SectionTitle("薄弱项", "选择章节学习后，练习反馈会继续帮助你发现薄弱项。")
                    ChipRow(state.recentWeaknesses, emptyText = "暂无薄弱项")
                }
            }
        }
    }
}

@Composable
private fun LevelDropdownPill(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val popupOffsetY = with(LocalDensity.current) { 42.dp.roundToPx() }
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.16f))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "CEFR $selectedLevel",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text("⌄", color = Color.White, fontWeight = FontWeight.Bold)
        }
        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, popupOffsetY),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.42f), RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        cefrLevels.forEach { level ->
                            val selected = level == selectedLevel
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Color(0xFFEAF3FF) else Color.Transparent)
                                    .clickable {
                                        onLevelSelected(level)
                                        expanded = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "CEFR $level",
                                    color = if (selected) AppBlue else AppInk,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                )
                                if (selected) {
                                    Text("已选", color = AppBlue, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyllabusModuleCard(index: Int, module: SyllabusModule, onStart: () -> Unit) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (index == 1) AppBlue else Color(0xFFEAF3FF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                text = index.toString().padStart(2, '0'),
                color = if (index == 1) Color.White else AppBlue,
                fontWeight = FontWeight.Bold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(module.title, color = AppInk, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(module.goal, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
                module.lessons.forEach { lesson ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        Text("•", color = AppGreen, fontWeight = FontWeight.Bold)
                        Text(lesson, color = AppInk, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        PrimaryAction("学习本章节", onClick = onStart)
    }
}

private val cefrLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2")

private val fallbackSyllabuses = listOf(
    CourseSyllabus(
        level = "A1",
        title = "A1 入门基础",
        description = "建立简单日常场景中的基础英语表达能力。",
        modules = listOf(
            SyllabusModule("自我介绍", "说明自己是谁，并提出简单问题。", listOf("be 动词与主语代词", "姓名、国家、职业", "写一段简短自我介绍")),
            SyllabusModule("日常生活", "谈论日常安排和熟悉物品。", listOf("一般现在时肯定句与否定句", "时间、星期和基础频率副词", "描述普通的一天")),
            SyllabusModule("地点与需求", "完成简单的旅行、购物和问路交流。", listOf("There is / There are 句型", "价格、地点和方向表达", "写一段简短请求")),
        ),
    ),
    CourseSyllabus(
        level = "A2",
        title = "A2 初级交流",
        description = "扩展日常表达，能够谈论过去经历和未来计划。",
        modules = listOf(
            SyllabusModule("过去经历", "描述简单的过去事件。", listOf("一般过去时规则与不规则动词", "旅行和周末活动词汇", "写一段简短旅行记录")),
            SyllabusModule("计划与偏好", "谈论打算、喜好和比较。", listOf("going to 与 would like", "比较级和最高级", "选择并解释自己的偏好")),
            SyllabusModule("社交场景", "处理邀请、建议和简单问题。", listOf("can、should、have to", "健康、食物和预约表达", "写一条礼貌消息")),
        ),
    ),
    CourseSyllabus(
        level = "B1",
        title = "B1 独立表达核心",
        description = "围绕工作、学习和旅行话题进行清晰沟通。",
        modules = listOf(
            SyllabusModule("故事与观点", "解释事件并给出原因。", listOf("过去进行时与现在完成时", "原因、转折连接词", "写一段个人经历")),
            SyllabusModule("工作与学习", "讨论目标、职责和进展。", listOf("义务和建议类情态动词", "职场常用词汇", "写一封进度更新邮件")),
            SyllabusModule("解决问题", "描述问题并提出可行方案。", listOf("第一条件句", "服务和投诉表达", "写一段解决方案")),
        ),
    ),
    CourseSyllabus(
        level = "B2",
        title = "B2 中高级流利度",
        description = "用更有层次、结构和自信的方式进行口头与书面表达。",
        modules = listOf(
            SyllabusModule("论证表达", "用证据构建平衡、有说服力的观点。", listOf("复杂从句与让步表达", "观点和辩论词汇", "写一篇正反观点短文")),
            SyllabusModule("职业沟通", "处理会议、提案和反馈。", listOf("被动语态与转述动词", "商务搭配表达", "写一段提案摘要")),
            SyllabusModule("媒体与社会", "理解较长文本和隐含观点。", listOf("定语从句与名词化", "媒体素养词汇", "总结一篇文章")),
        ),
    ),
    CourseSyllabus(
        level = "C1",
        title = "C1 高级精准表达",
        description = "在高要求场景中提升风格、准确性和语域控制。",
        modules = listOf(
            SyllabusModule("学术风格", "写出结构清晰、表达精准的分析。", listOf("委婉表达与立场标记", "数据和研究类词汇", "写一段分析型段落")),
            SyllabusModule("细腻讨论", "表达微妙的赞同、怀疑和批评。", listOf("倒装与强调", "地道观点表达", "回应复杂话题")),
            SyllabusModule("专业润色", "为高风险沟通调整语气。", listOf("语域与外交式表达", "领导力和谈判词汇", "改写文本以提升清晰度和分寸感")),
        ),
    ),
    CourseSyllabus(
        level = "C2",
        title = "C2 精通与掌控",
        description = "以接近母语者的灵活度和修辞控制力组织语言。",
        modules = listOf(
            SyllabusModule("修辞控制", "有意识地使用强调、节奏和叙述框架。", listOf("高级语篇标记", "修辞手法", "写一个有说服力的开头")),
            SyllabusModule("专业阅读", "处理密集、抽象或技术性材料。", listOf("复杂名词短语", "专业术语处理策略", "产出专家级摘要")),
            SyllabusModule("编辑写作", "围绕声音、精确度和影响力进行修改。", listOf("风格转换与简洁表达", "隐喻和细微差别", "修改一篇高阶文章")),
        ),
    ),
)
