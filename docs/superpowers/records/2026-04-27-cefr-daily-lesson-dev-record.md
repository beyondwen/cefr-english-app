# CEFR 日课闭环开发记录（2026-04-27）

## 1. 本次开发目标

把原来的演示型原型升级为可自用的「每日一课」闭环，围绕以下主链路收敛：

1. 首次测级，确定起始 `CEFR` 等级。
2. 首页直接进入今日课程，而不是每次重新测级。
3. 课程由「模板定义骨架 + AI 填充内容」生成。
4. 同一课默认缓存固定，支持手动「换一版」。
5. 完成阅读、语法、短写作后提交，系统自动推进下一课。
6. 进度页展示真实摘要，方便持续使用。

## 2. 关联文档

- 规格文档：`docs/superpowers/specs/2026-04-27-cefr-daily-lesson-design.md`
- 实现计划：`docs/superpowers/plans/2026-04-27-cefr-daily-lesson-implementation.md`

如果下次继续开发，先读规格，再看实现计划，最后看本文档确认当前代码状态和遗留事项。

## 3. 本次提交

- `a7e41aa feat(worker): 增加日课模板与实例模型骨架（任务 1/6）`
- `0719a93 feat(worker): 实现今日课程生成与缓存流程（任务 2/6）`
- `3972525 feat(worker): 完成日课提交流程与进度推进（任务 3/6）`
- `81c1e61 feat(android): 增加首页与日课闭环交互（任务 4-6）`

## 4. 当前已完成的能力

### 4.1 Worker

- 新增课程模板域模型，覆盖 `A1 / A2 / B1`。
- 新增课程实例、用户画像、提交结果等类型。
- 数据层增加 `lesson_instances`、`lesson_submissions` 和进度相关字段。
- 支持 `GET /api/today-lesson`：
  - 有未完成课程时返回缓存实例；
  - 没有未完成课程时按模板生成新实例。
- 支持 `POST /api/today-lesson/regenerate`：
  - 对当前未完成课程生成新版本并覆盖当前学习版本。
- 支持 `POST /api/lesson/submit`：
  - 校验阅读、语法和写作；
  - 保存写作反馈；
  - 满足完成条件后推进到下一课。
- `GET /api/me/summary` 现在会返回：
  - `currentLevel`
  - `currentLessonId`
  - `completedCount`
  - `nextLessonId`
  - `todayCompleted`
  - `recentWeaknesses`

### 4.2 Android

- 起始入口从 `Placement` 改成 `Home`。
- 新增首页：
  - 展示当前等级、当前课、已完成数量、弱项；
  - 未开始时进入测级；
  - 已有课时继续学习；
  - 今日完成后进入进度页。
- 课程页改成真实日课交互：
  - 展示阅读材料与阅读题；
  - 展示语法讲解与语法题；
  - 展示短写作题与写作要求；
  - 支持「换一版」；
  - 支持提交整课。
- 测级页从硬编码按钮改成最小输入表单。
- 进度页现在展示真实摘要字段，而不只是完成数。

## 5. 当前主流程

### 5.1 首次使用

1. 首页发现没有当前课程且完成数为 `0`。
2. 引导进入测级页。
3. 测级完成后回到首页。
4. 首页继续进入今日课程。

### 5.2 日课学习

1. 打开课程页后，客户端请求 `GET /api/today-lesson?userId=...`。
2. Worker 返回当前未完成课程，或生成新的课程实例。
3. 用户填写阅读题、语法题和短写作。
4. 提交后调用 `POST /api/lesson/submit`。
5. 后端保存提交与写作反馈，并在满足完成条件后推进进度。
6. 客户端进入进度页查看状态。

## 6. 关键代码位置

### 6.1 Worker

- 模板定义：`worker/src/domain/lessonTemplates.ts`
- 核心类型：`worker/src/domain/types.ts`
- 今日课程状态机：`worker/src/services/todayLessonService.ts`
- 提交流程与推进：`worker/src/services/progressService.ts`
- 写作反馈：`worker/src/services/writingReviewService.ts`
- 今日课程接口：`worker/src/routes/lessons.ts`
- 提交接口：`worker/src/routes/lessonSubmit.ts`
- 进度摘要：`worker/src/routes/progress.ts`

### 6.2 Android

- 入口与路由：`android/app/src/main/java/com/wenha/cefrenglish/app/CefrEnglishApp.kt`
- 首页：`android/app/src/main/java/com/wenha/cefrenglish/ui/home/`
- 课程页：`android/app/src/main/java/com/wenha/cefrenglish/ui/lesson/`
- 测级页：`android/app/src/main/java/com/wenha/cefrenglish/ui/placement/`
- 进度页：`android/app/src/main/java/com/wenha/cefrenglish/ui/progress/`
- API 契约：`android/app/src/main/java/com/wenha/cefrenglish/data/api/`
- 仓储映射：`android/app/src/main/java/com/wenha/cefrenglish/data/`

## 7. 验证方式

### 7.1 Worker

```bash
rtk node /home/wenha/project/AndroidWork/cefr-english-app/worker/node_modules/vitest/vitest.mjs run /home/wenha/project/AndroidWork/cefr-english-app/worker/test
```

结果：`12` 个测试文件、`19` 个测试全部通过。

### 7.2 Android

```bash
rtk env GRADLE_USER_HOME=/tmp/gradle-home gradle -p /home/wenha/project/AndroidWork/cefr-english-app/android :app:compileDebugKotlin --no-daemon --console=plain
```

结果：编译通过。

```bash
rtk env GRADLE_USER_HOME=/tmp/gradle-home gradle -p /home/wenha/project/AndroidWork/cefr-english-app/android testDebugUnitTest --no-daemon --console=plain
```

结果：`testDebugUnitTest` 通过。

## 8. 本地环境说明

- 当前环境下可以直接使用全局 `Gradle`。
- 为了避免本机环境污染，Android 验证统一使用：
  - `GRADLE_USER_HOME=/tmp/gradle-home`
- 在沙箱里直接跑 `Gradle` 可能会遇到：
  - `Could not determine a usable wildcard IP for this machine`
- 这个问题不是项目代码错误，而是环境限制；出沙箱后可正常运行。

## 9. 目前仍然存在的缺口

- `AI provider` 仍然是占位实现，不是真实模型接入。
- 测级仍然是最小输入表单，不是真实题库式测级。
- 课程题型仍然偏基础，暂无更复杂的判题策略。
- 目前进度体系是单用户本地 `userId`，没有登录和多设备同步。
- 首页和进度页已经可用，但还没有做更细的连续学习统计和补学策略。

## 10. 下次开发建议顺序

建议按下面顺序继续，而不是同时发散：

1. 接真实 AI 服务，把 `fakeAiProvider` 替换掉。
2. 强化课程模板：
   - 增加更稳定的主题池；
   - 增加更明确的语法目标；
   - 增加更合理的题目结构。
3. 把测级从输入表单升级为真实测级题流。
4. 优化课程提交后的反馈展示，尤其是写作点评和参考改写的可读性。
5. 如果开始长期自用，再考虑本地缓存、离线支持和多设备同步。

## 11. 下次继续开发前建议先做什么

1. 先看 `git log --oneline`，确认是否已经基于 `81c1e61` 之后继续开发。
2. 先读规格和实现计划，避免重新讨论已经定下来的产品边界。
3. 启动 Worker 并跑 Android 单测，确认环境没漂移。
4. 如果要改 API 契约，先同步修改：
   - `worker/src/domain/types.ts`
   - `worker/src/routes/*`
   - `android/app/src/main/java/com/wenha/cefrenglish/data/api/*`
   - `android/app/src/main/java/com/wenha/cefrenglish/data/*`

## 12. 一句话总结

截至本次开发，这个项目已经从「演示型原型」升级为一个可以继续往自用产品推进的「每日一课闭环骨架」。
