# CEFR 日课闭环实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将当前演示型 CEFR 英语学习原型升级为可自用的「每日一课」闭环，支持模板驱动的 AI 课程生成、课程缓存、手动换一版、真实完成判定和正确的下一课推进。

**架构：** Worker 端新增课程模板与课程实例模型，将现有 `lessons/progress/writing` 三条松散链路收敛为「今日课程 -> 提交完成 -> 推进下一课」状态机。Android 端新增首页入口和真实课程交互页，围绕 `today-lesson` 与 `lesson/submit` 两个核心接口重建 UI 流程。

**技术栈：** Cloudflare Workers、TypeScript、Vitest、Kotlin、Jetpack Compose、Retrofit、DataStore

---

## 文件结构

### Worker

- 创建：`worker/src/domain/lessonTemplates.ts`
  - 固定模板定义，覆盖至少 `A1/A2/B1` 的主线课程骨架。
- 创建：`worker/src/services/todayLessonService.ts`
  - 负责「获取今日课程 / 生成缓存 / 换一版」的核心状态机。
- 创建：`worker/test/todayLessonService.test.ts`
  - 覆盖生成、缓存命中、换一版、主题轮换等核心逻辑。
- 创建：`worker/test/todayLessonRoute.test.ts`
  - 覆盖 `GET /api/today-lesson` 与 `POST /api/today-lesson/regenerate`。
- 修改：`worker/src/domain/types.ts`
  - 增加模板、课程实例、提交载荷、摘要 DTO 类型。
- 修改：`worker/src/providers/aiProvider.ts`
  - 将 AI 输入升级为模板 + 弱项 + 主题轮换，输出阅读题和语法题。
- 修改：`worker/src/repositories/userRepository.ts`
  - 承载 `recentWeaknesses`、`themeRotationState`、`currentTemplateId`。
- 修改：`worker/src/repositories/lessonRepository.ts`
  - 从按 lessonId 查缓存扩展为按用户当前模板和版本管理实例。
- 修改：`worker/src/repositories/progressRepository.ts`
  - 管理进行中实例、完成记录、摘要。
- 修改：`worker/src/repositories/writingReviewRepository.ts`
  - 写作反馈与提交结果持久化。
- 修改：`worker/src/routes/placement.ts`
  - 测级后初始化用户画像与起始模板指针。
- 修改：`worker/src/routes/lessons.ts`
  - 逐步迁移或转发到新的 `today-lesson` 逻辑。
- 修改：`worker/src/routes/progress.ts`
  - 摘要返回当前课、下一课和今日完成状态。
- 修改：`worker/src/routes/writing.ts`
  - 如保留旧接口，则改为委托新的提交逻辑或标记废弃。
- 修改：`worker/src/app.ts`
  - 注册 `GET /api/today-lesson`、`POST /api/today-lesson/regenerate`、`POST /api/lesson/submit`。
- 修改：`worker/src/services/placementService.ts`
  - 输出更稳定的起始等级和弱项。
- 修改：`worker/src/services/progressService.ts`
  - 从单纯 completed ids 计算升级为完整的日课推进规则。
- 修改：`worker/src/services/writingReviewService.ts`
  - 返回轻量完成门槛 + AI 反馈。
- 修改：`worker/test/e2eFlow.test.ts`
  - 改为 `placement -> today-lesson -> submit -> summary` 闭环。
- 修改：`worker/test/helpers/fakeDb.ts`
  - 扩展假库结构以支持模板、实例和摘要。
- 修改：`worker/migrations/0001_init.sql`
  - 增补 `user_profile`、`lesson_instances`、`lesson_submissions` 所需字段或表。

### Android

- 创建：`android/app/src/main/java/com/wenha/cefrenglish/ui/home/HomeScreen.kt`
  - 首页展示今日课程、等级、弱项和继续学习入口。
- 创建：`android/app/src/main/java/com/wenha/cefrenglish/ui/home/HomeViewModel.kt`
  - 首页状态获取与路由决策。
- 创建：`android/app/src/test/java/com/wenha/cefrenglish/ui/home/HomeViewModelTest.kt`
  - 首页状态与继续学习逻辑测试。
- 创建：`android/app/src/test/java/com/wenha/cefrenglish/testdoubles/FakeHomeRepository.kt`
  - 首页仓储测试替身。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/data/api/AppApi.kt`
  - 增加 `today-lesson`、`regenerate`、`lesson/submit`。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/data/api/ApiModels.kt`
  - 增加首页、课程实例、提交结果的 DTO。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/domain/models.kt`
  - 增加 `DailyLesson`、`LessonQuestion`、`LessonSummary`、`LessonSubmissionResult`。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/data/LessonRepository.kt`
  - 从 `load + submitWriting + completeLesson` 调整为 `getTodayLesson + regenerate + submitLesson`。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/data/ProgressRepository.kt`
  - 与首页摘要契约对齐。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/app/AppContainer.kt`
  - 注入首页与日课所需仓储。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/lesson/LessonViewModel.kt`
  - 增加题目状态、写作验证、提交结果和换一版流程。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/lesson/LessonScreen.kt`
  - 渲染阅读题、语法题、短写作、反馈和换一版按钮。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/progress/ProgressViewModel.kt`
  - 展示当前课、下一课、已完成数量和今日完成状态。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/progress/ProgressScreen.kt`
  - 与真实摘要对齐。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/placement/PlacementViewModel.kt`
  - 支持首次进入后跳转首页。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/placement/PlacementScreen.kt`
  - 从演示按钮升级为真实测级表单的最小版本。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/navigation/AppNavGraph.kt`
  - 增加 `Home` 路由并调整默认入口。
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/app/CefrEnglishApp.kt`
  - 根据用户画像和首页状态决定起始页面。
- 修改：`android/app/src/test/java/com/wenha/cefrenglish/testdoubles/FakeLessonRepository.kt`
  - 支持今日课程、换一版、提交结果。
- 修改：`android/app/src/test/java/com/wenha/cefrenglish/testdoubles/FakeProgressRepository.kt`
  - 返回新的摘要结构。
- 修改：`android/app/src/test/java/com/wenha/cefrenglish/ui/lesson/LessonViewModelTest.kt`
  - 覆盖完成门槛、换一版和提交反馈。
- 修改：`android/app/src/test/java/com/wenha/cefrenglish/ui/progress/ProgressViewModelTest.kt`
  - 覆盖新的摘要字段。
- 修改：`android/app/src/test/java/com/wenha/cefrenglish/ui/navigation/AppNavGraphTest.kt`
  - 起始路由从 `Placement` 更新为条件路由测试。

## 任务 1：重建 Worker 域模型与数据库骨架

**文件：**
- 创建：`worker/src/domain/lessonTemplates.ts`
- 修改：`worker/src/domain/types.ts`
- 修改：`worker/migrations/0001_init.sql`
- 修改：`worker/test/helpers/fakeDb.ts`
- 测试：`worker/test/todayLessonService.test.ts`

- [ ] **步骤 1：先写模板与类型的失败测试**

```ts
import { describe, expect, it } from 'vitest'
import { lessonTemplatesByLevel } from '../src/domain/lessonTemplates'

describe('lesson templates', () => {
  it('contains at least 2 templates for each A1/A2/B1 level', () => {
    expect(lessonTemplatesByLevel.A1.length).toBeGreaterThanOrEqual(2)
    expect(lessonTemplatesByLevel.A2.length).toBeGreaterThanOrEqual(2)
    expect(lessonTemplatesByLevel.B1.length).toBeGreaterThanOrEqual(2)
  })
})
```

- [ ] **步骤 2：运行测试验证失败**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/worker && npm test -- --run worker/test/todayLessonService.test.ts`

预期：FAIL，报错 `Cannot find module '../src/domain/lessonTemplates'` 或缺少导出。

- [ ] **步骤 3：实现模板与类型骨架**

```ts
export type LessonTemplate = {
  templateId: string
  level: 'A1' | 'A2' | 'B1'
  sequence: number
  grammarFocus: string
  readingTaskType: 'reading_mcq'
  writingTaskType: 'short_paragraph'
  themePool: string[]
  targetWordRange: { min: number; max: number }
  questionCounts: { reading: number; grammar: number }
}

export const lessonTemplatesByLevel: Record<'A1' | 'A2' | 'B1', LessonTemplate[]> = {
  A1: [/* ... */],
  A2: [/* ... */],
  B1: [/* ... */],
}
```

- [ ] **步骤 4：扩展数据库迁移与 fakeDb 结构**

```sql
ALTER TABLE progress ADD COLUMN current_template_id TEXT;
ALTER TABLE progress ADD COLUMN current_lesson_instance_id TEXT;
ALTER TABLE progress ADD COLUMN today_completed INTEGER NOT NULL DEFAULT 0;

CREATE TABLE lesson_instances (
  lesson_instance_id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  template_id TEXT NOT NULL,
  generation_version INTEGER NOT NULL,
  status TEXT NOT NULL,
  lesson_json TEXT NOT NULL,
  generated_at TEXT NOT NULL,
  completed_at TEXT
);
```

- [ ] **步骤 5：运行模板测试验证通过**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/worker && npm test -- --run worker/test/todayLessonService.test.ts`

预期：PASS，模板与类型测试通过。

- [ ] **步骤 6：Commit**

```bash
cd /home/wenha/project/AndroidWork/cefr-english-app
git add worker/src/domain/lessonTemplates.ts worker/src/domain/types.ts worker/migrations/0001_init.sql worker/test/helpers/fakeDb.ts worker/test/todayLessonService.test.ts
git commit -m "feat(worker): 增加日课模板与实例模型骨架"
```

## 任务 2：实现今日课程生成、缓存与换一版

**文件：**
- 创建：`worker/src/services/todayLessonService.ts`
- 修改：`worker/src/providers/aiProvider.ts`
- 修改：`worker/src/repositories/userRepository.ts`
- 修改：`worker/src/repositories/lessonRepository.ts`
- 修改：`worker/src/routes/lessons.ts`
- 修改：`worker/src/app.ts`
- 创建：`worker/test/todayLessonRoute.test.ts`
- 测试：`worker/test/todayLessonService.test.ts`

- [ ] **步骤 1：写生成与缓存的失败测试**

```ts
it('returns the cached lesson instance when an unfinished lesson already exists', async () => {
  const result = await getTodayLesson({
    userId: 'u1',
    profileRepo,
    lessonRepo,
    aiProvider,
  })

  expect(result.lessonInstanceId).toBe('instance-1')
  expect(aiProvider.generateLesson).toHaveBeenCalledTimes(0)
})

it('regenerates a new version for an unfinished lesson', async () => {
  const regenerated = await regenerateTodayLesson({ userId: 'u1', profileRepo, lessonRepo, aiProvider })
  expect(regenerated.generationVersion).toBe(2)
})
```

- [ ] **步骤 2：运行测试验证失败**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/worker && npm test -- --run worker/test/todayLessonService.test.ts worker/test/todayLessonRoute.test.ts`

预期：FAIL，缺少 `getTodayLesson` / `regenerateTodayLesson` 实现。

- [ ] **步骤 3：实现今日课程服务**

```ts
export const getTodayLesson = async (input: TodayLessonInput): Promise<LessonInstance> => {
  const existing = await input.lessonRepo.findActiveByUserId(input.userId)
  if (existing) return existing

  const profile = await input.profileRepo.getOrCreate(input.userId)
  const template = pickCurrentTemplate(profile.currentLevel, profile.currentTemplateId)
  const generated = await input.aiProvider.generateLesson({
    level: profile.currentLevel,
    lessonId: template.templateId,
    grammarFocus: template.grammarFocus,
    writingTask: template.writingTaskType,
    weaknesses: profile.recentWeaknesses,
    theme: chooseTheme(profile.themeRotationState, template.themePool),
  })

  return input.lessonRepo.insertActiveInstance(profile, template, generated)
}
```

- [ ] **步骤 4：注册并测试新接口**

```ts
if (url.pathname === '/api/today-lesson' && request.method === 'GET') return handleTodayLesson(request, env)
if (url.pathname === '/api/today-lesson/regenerate' && request.method === 'POST') return handleRegenerateTodayLesson(request, env)
```

- [ ] **步骤 5：运行 Worker 接口测试验证通过**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/worker && npm test -- --run worker/test/todayLessonService.test.ts worker/test/todayLessonRoute.test.ts`

预期：PASS，缓存命中、首次生成和换一版均通过。

- [ ] **步骤 6：Commit**

```bash
cd /home/wenha/project/AndroidWork/cefr-english-app
git add worker/src/services/todayLessonService.ts worker/src/providers/aiProvider.ts worker/src/repositories/userRepository.ts worker/src/repositories/lessonRepository.ts worker/src/routes/lessons.ts worker/src/app.ts worker/test/todayLessonService.test.ts worker/test/todayLessonRoute.test.ts
git commit -m "feat(worker): 实现今日课程生成与缓存流程"
```

## 任务 3：实现课程提交、完成判定与摘要推进

**文件：**
- 修改：`worker/src/services/writingReviewService.ts`
- 修改：`worker/src/services/progressService.ts`
- 修改：`worker/src/routes/progress.ts`
- 修改：`worker/src/routes/writing.ts`
- 创建：`worker/src/routes/lessonSubmit.ts`
- 修改：`worker/src/repositories/progressRepository.ts`
- 修改：`worker/src/repositories/writingReviewRepository.ts`
- 修改：`worker/test/e2eFlow.test.ts`
- 测试：`worker/test/progressService.test.ts`

- [ ] **步骤 1：先写失败测试，覆盖完成条件和推进**

```ts
it('does not complete the lesson when writing has fewer than 2 sentences', async () => {
  const result = await submitLesson({ /* ... */ writingSubmission: 'I like coffee.' })
  expect(result.completed).toBe(false)
  expect(result.missingRequirements).toContain('writing_min_sentences')
})

it('completes the lesson and advances to the next template', async () => {
  const result = await submitLesson({ /* ... */ writingSubmission: 'I like coffee. I drink it every morning.' })
  expect(result.completed).toBe(true)
  expect(result.nextTemplateId).toBe('A1-02')
})
```

- [ ] **步骤 2：运行测试验证失败**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/worker && npm test -- --run worker/test/progressService.test.ts worker/test/e2eFlow.test.ts`

预期：FAIL，缺少 `submitLesson` 路径或返回结构不符。

- [ ] **步骤 3：实现提交结果结构与完成判定**

```ts
const sentenceCount = trimmed.length === 0 ? 0 : trimmed.split(/[.!?]+/).filter(Boolean).length
const completed =
  readingAnswers.length === expectedReadingCount &&
  grammarAnswers.length === expectedGrammarCount &&
  sentenceCount >= 2

return {
  completed,
  writingReview,
  missingRequirements: completed ? [] : ['writing_min_sentences'],
}
```

- [ ] **步骤 4：实现摘要接口新结构**

```ts
return json({
  currentLevel: profile.currentLevel,
  currentLessonId: activeLesson?.templateId ?? null,
  completedCount: summary.completedCount,
  nextLessonId: summary.nextTemplateId,
  todayCompleted: summary.todayCompleted,
  recentWeaknesses: profile.recentWeaknesses,
})
```

- [ ] **步骤 5：运行 Worker 闭环测试验证通过**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/worker && npm test -- --run worker/test/progressService.test.ts worker/test/e2eFlow.test.ts worker/test/writingReviewService.test.ts`

预期：PASS，`today-lesson -> submit -> summary` 闭环通过。

- [ ] **步骤 6：Commit**

```bash
cd /home/wenha/project/AndroidWork/cefr-english-app
git add worker/src/services/writingReviewService.ts worker/src/services/progressService.ts worker/src/routes/progress.ts worker/src/routes/writing.ts worker/src/routes/lessonSubmit.ts worker/src/repositories/progressRepository.ts worker/src/repositories/writingReviewRepository.ts worker/test/progressService.test.ts worker/test/e2eFlow.test.ts
git commit -m "feat(worker): 完成日课提交流程与进度推进"
```

## 任务 4：调整 Android 数据契约与首页入口

**文件：**
- 创建：`android/app/src/main/java/com/wenha/cefrenglish/ui/home/HomeScreen.kt`
- 创建：`android/app/src/main/java/com/wenha/cefrenglish/ui/home/HomeViewModel.kt`
- 创建：`android/app/src/test/java/com/wenha/cefrenglish/ui/home/HomeViewModelTest.kt`
- 创建：`android/app/src/test/java/com/wenha/cefrenglish/testdoubles/FakeHomeRepository.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/data/api/AppApi.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/data/api/ApiModels.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/domain/models.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/app/AppContainer.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/navigation/AppNavGraph.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/app/CefrEnglishApp.kt`
- 测试：`android/app/src/test/java/com/wenha/cefrenglish/ui/navigation/AppNavGraphTest.kt`

- [ ] **步骤 1：先写首页状态和导航失败测试**

```kotlin
@Test
fun loadSummary_setsCurrentLessonAndWeaknesses() = runTest {
    val repository = FakeHomeRepository(
        summary = LessonSummary(
            currentLevel = "A2",
            currentLessonId = "A2-01",
            completedCount = 3,
            nextLessonId = "A2-02",
            todayCompleted = false,
            recentWeaknesses = listOf("grammar"),
        ),
    )
    val viewModel = HomeViewModel(repository)

    viewModel.refresh("u1")

    assertEquals("A2-01", viewModel.uiState.value.currentLessonId)
}
```

- [ ] **步骤 2：运行 Android 单测验证失败**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/android && ./gradlew testDebugUnitTest --tests com.wenha.cefrenglish.ui.home.HomeViewModelTest --tests com.wenha.cefrenglish.ui.navigation.AppNavGraphTest`

预期：FAIL，缺少 `HomeViewModel` 或新的 `LessonSummary`。

- [ ] **步骤 3：实现首页模型和 API DTO**

```kotlin
data class LessonSummary(
    val currentLevel: String,
    val currentLessonId: String?,
    val completedCount: Int,
    val nextLessonId: String?,
    val todayCompleted: Boolean,
    val recentWeaknesses: List<String>,
)
```

- [ ] **步骤 4：将起始路由改为首页优先**

```kotlin
sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Placement : AppRoute("placement")
    data object Lesson : AppRoute("lesson")
    data object Progress : AppRoute("progress")
}
```

- [ ] **步骤 5：运行首页与导航测试验证通过**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/android && ./gradlew testDebugUnitTest --tests com.wenha.cefrenglish.ui.home.HomeViewModelTest --tests com.wenha.cefrenglish.ui.navigation.AppNavGraphTest`

预期：PASS，首页状态和路由测试通过。

- [ ] **步骤 6：Commit**

```bash
cd /home/wenha/project/AndroidWork/cefr-english-app
git add android/app/src/main/java/com/wenha/cefrenglish/ui/home android/app/src/test/java/com/wenha/cefrenglish/ui/home android/app/src/test/java/com/wenha/cefrenglish/testdoubles/FakeHomeRepository.kt android/app/src/main/java/com/wenha/cefrenglish/data/api/AppApi.kt android/app/src/main/java/com/wenha/cefrenglish/data/api/ApiModels.kt android/app/src/main/java/com/wenha/cefrenglish/domain/models.kt android/app/src/main/java/com/wenha/cefrenglish/app/AppContainer.kt android/app/src/main/java/com/wenha/cefrenglish/ui/navigation/AppNavGraph.kt android/app/src/main/java/com/wenha/cefrenglish/app/CefrEnglishApp.kt
git commit -m "feat(android): 增加首页入口与日课摘要模型"
```

## 任务 5：重做课程页，支持真实完成和换一版

**文件：**
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/data/LessonRepository.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/lesson/LessonViewModel.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/lesson/LessonScreen.kt`
- 修改：`android/app/src/test/java/com/wenha/cefrenglish/testdoubles/FakeLessonRepository.kt`
- 修改：`android/app/src/test/java/com/wenha/cefrenglish/ui/lesson/LessonViewModelTest.kt`
- 测试：`android/app/src/test/java/com/wenha/cefrenglish/ui/lesson/LessonViewModelTest.kt`

- [ ] **步骤 1：先写课程完成条件和换一版失败测试**

```kotlin
@Test
fun submitLesson_requiresAtLeastTwoSentences() = runTest {
    val repository = FakeLessonRepository()
    val viewModel = LessonViewModel(repository)

    viewModel.loadTodayLesson("u1")
    viewModel.updateWriting("I like coffee.")
    viewModel.submitLesson("u1")

    assertEquals(false, viewModel.uiState.value.completed)
}

@Test
fun regenerateLesson_replacesLessonVersion() = runTest {
    val repository = FakeLessonRepository()
    val viewModel = LessonViewModel(repository)

    viewModel.loadTodayLesson("u1")
    val firstId = viewModel.uiState.value.lessonInstanceId
    viewModel.regenerate("u1")

    assertNotEquals(firstId, viewModel.uiState.value.lessonInstanceId)
}
```

- [ ] **步骤 2：运行 Android 单测验证失败**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/android && ./gradlew testDebugUnitTest --tests com.wenha.cefrenglish.ui.lesson.LessonViewModelTest`

预期：FAIL，现有 `LessonViewModel` 无法表达题目状态与提交结果。

- [ ] **步骤 3：实现新的仓储接口与 ViewModel 状态**

```kotlin
interface LessonRepository {
    suspend fun getTodayLesson(userId: String): DailyLesson
    suspend fun regenerateTodayLesson(userId: String): DailyLesson
    suspend fun submitLesson(
        userId: String,
        lessonInstanceId: String,
        readingAnswers: List<String>,
        grammarAnswers: List<String>,
        writingSubmission: String,
    ): LessonSubmissionResult
}
```

- [ ] **步骤 4：扩展课程页 UI**

```kotlin
Button(onClick = { viewModel.regenerate(userId) }) { Text("换一版") }
Button(
    enabled = state.canSubmit,
    onClick = { viewModel.submitLesson(userId) },
) { Text("提交并完成本课") }
```

- [ ] **步骤 5：运行课程页单测验证通过**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/android && ./gradlew testDebugUnitTest --tests com.wenha.cefrenglish.ui.lesson.LessonViewModelTest`

预期：PASS，完成门槛、换一版和提交结果测试通过。

- [ ] **步骤 6：Commit**

```bash
cd /home/wenha/project/AndroidWork/cefr-english-app
git add android/app/src/main/java/com/wenha/cefrenglish/data/LessonRepository.kt android/app/src/main/java/com/wenha/cefrenglish/ui/lesson/LessonViewModel.kt android/app/src/main/java/com/wenha/cefrenglish/ui/lesson/LessonScreen.kt android/app/src/test/java/com/wenha/cefrenglish/testdoubles/FakeLessonRepository.kt android/app/src/test/java/com/wenha/cefrenglish/ui/lesson/LessonViewModelTest.kt
git commit -m "feat(android): 完成日课课程页交互重构"
```

## 任务 6：收尾测级入口、进度页与全链路验证

**文件：**
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/placement/PlacementViewModel.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/placement/PlacementScreen.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/progress/ProgressViewModel.kt`
- 修改：`android/app/src/main/java/com/wenha/cefrenglish/ui/progress/ProgressScreen.kt`
- 修改：`android/app/src/test/java/com/wenha/cefrenglish/ui/progress/ProgressViewModelTest.kt`
- 修改：`android/app/src/test/java/com/wenha/cefrenglish/ui/placement/PlacementViewModelTest.kt`
- 测试：`worker/test/e2eFlow.test.ts`

- [ ] **步骤 1：先写摘要与首次测级跳转失败测试**

```kotlin
@Test
fun refresh_setsCurrentLessonAndTodayCompleted() = runTest {
    val repository = FakeProgressRepository()
    val viewModel = ProgressViewModel(repository)

    viewModel.refresh("u1")

    assertEquals("A1-02", viewModel.uiState.value.nextLessonId)
    assertEquals(false, viewModel.uiState.value.todayCompleted)
}
```

- [ ] **步骤 2：运行 Android + Worker 目标测试验证失败**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/android && ./gradlew testDebugUnitTest --tests com.wenha.cefrenglish.ui.progress.ProgressViewModelTest --tests com.wenha.cefrenglish.ui.placement.PlacementViewModelTest`

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/worker && npm test -- --run worker/test/e2eFlow.test.ts`

预期：FAIL，进度字段或首次测级后的首页逻辑尚未对齐。

- [ ] **步骤 3：实现首次测级后进入首页、进度摘要与今天完成态**

```kotlin
data class ProgressUiState(
    val completedCount: Int = 0,
    val nextLessonId: String? = null,
    val currentLessonId: String? = null,
    val currentLevel: String = "",
    val todayCompleted: Boolean = false,
)
```

- [ ] **步骤 4：运行完整验证**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/worker && npm test`

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/android && ./gradlew testDebugUnitTest`

预期：PASS，Worker 与 Android 单测全部通过。

- [ ] **步骤 5：手动联调验证**

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/worker && npm install && npm run dev`

运行：`cd /home/wenha/project/AndroidWork/cefr-english-app/android && ./gradlew assembleDebug`

预期：
- Worker 本地启动成功。
- Android Debug 包构建成功。
- 模拟器中可完成 `测级 -> 今日课程 -> 提交 -> 进度更新`。

- [ ] **步骤 6：Commit**

```bash
cd /home/wenha/project/AndroidWork/cefr-english-app
git add android/app/src/main/java/com/wenha/cefrenglish/ui/placement/PlacementViewModel.kt android/app/src/main/java/com/wenha/cefrenglish/ui/placement/PlacementScreen.kt android/app/src/main/java/com/wenha/cefrenglish/ui/progress/ProgressViewModel.kt android/app/src/main/java/com/wenha/cefrenglish/ui/progress/ProgressScreen.kt android/app/src/test/java/com/wenha/cefrenglish/ui/progress/ProgressViewModelTest.kt android/app/src/test/java/com/wenha/cefrenglish/ui/placement/PlacementViewModelTest.kt worker/test/e2eFlow.test.ts
git commit -m "feat(app): 打通 CEFR 日课闭环"
```

## 自检

- 规格覆盖度：
  - 模板驱动生成：任务 1、2 覆盖。
  - 首次生成缓存、手动换一版：任务 2 覆盖。
  - 阅读 + 语法 + 写作联合完成：任务 3、5 覆盖。
  - 首页今日课程入口：任务 4 覆盖。
  - 进度摘要与下一课推进：任务 3、6 覆盖。
  - 测级后不再默认回到测级页：任务 6 覆盖。
- 占位符扫描：
  - 已避免 `TODO`、`待定`、`后续实现` 等模糊条目。
  - 每个任务都给出了具体文件、测试命令和最小代码骨架。
- 类型一致性：
  - Worker 使用 `LessonTemplate`、`LessonInstance`、`LessonSubmissionResult`。
  - Android 对应使用 `DailyLesson`、`LessonSummary`、`LessonSubmissionResult`，避免直接暴露数据库结构。

## 执行交接

计划已完成并保存到 `docs/superpowers/plans/2026-04-27-cefr-daily-lesson-implementation.md`。两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点

选哪种方式？
