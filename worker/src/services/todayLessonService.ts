import { lessonTemplatesByLevel } from '../domain/lessonTemplates'
import type { LessonInstance, LessonQuestion, LessonTemplate, UserProfile } from '../domain/types'
import type { AiProvider } from '../providers/aiProvider'

const createLessonQuestions = (prompts: string[], prefix: string): LessonQuestion[] =>
  prompts.map((prompt, index) => ({
    questionId: `${prefix}-${index + 1}`,
    prompt,
    choices: [],
    answer: '',
  }))

const nextThemeRotationState = (current: UserProfile['themeRotationState']): UserProfile['themeRotationState'] =>
  current === 'life' ? 'expression' : 'life'

const createLessonInstanceId = (userId: string, templateId: string, version: number): string => `${userId}:${templateId}:v${version}`

const pickCurrentTemplate = (profile: UserProfile): LessonTemplate => {
  const templates = lessonTemplatesByLevel[profile.currentLevel]
  const matched = profile.currentTemplateId ? templates.find((item) => item.templateId === profile.currentTemplateId) : null
  return matched ?? templates[0]
}

const buildLessonInstance = (input: {
  userId: string
  template: LessonTemplate
  generated: Awaited<ReturnType<AiProvider['generateLesson']>>
  theme: string
  generationVersion: number
}): LessonInstance => ({
  lessonInstanceId: createLessonInstanceId(input.userId, input.template.templateId, input.generationVersion),
  userId: input.userId,
  templateId: input.template.templateId,
  level: input.template.level,
  theme: input.theme,
  readingText: input.generated.readingText,
  readingQuestions: createLessonQuestions(input.generated.readingQuestions, `${input.template.templateId}-reading`),
  grammarExplanation: input.generated.grammarExplanation,
  grammarQuestions: createLessonQuestions(input.generated.grammarQuestions, `${input.template.templateId}-grammar`),
  writingPrompt: input.generated.writingPrompt,
  writingRubric: [
    `Write ${input.template.targetWordRange.min}-${input.template.targetWordRange.max} words if possible.`,
    'Use 2-4 clear sentences.',
  ],
  status: 'generated',
  generationVersion: input.generationVersion,
  generatedAt: new Date().toISOString(),
  completedAt: null,
})

export const getTodayLesson = async (input: {
  userId: string
  profileRepo: {
    getOrCreate(userId: string): Promise<UserProfile>
    updateCurrentTemplate(userId: string, templateId: string, themeRotationState: UserProfile['themeRotationState']): Promise<void>
  }
  lessonRepo: {
    findActiveByUserId(userId: string): Promise<LessonInstance | null>
    insertActiveInstance(lesson: LessonInstance): Promise<void>
  }
  aiProvider: AiProvider
}): Promise<LessonInstance> => {
  const existing = await input.lessonRepo.findActiveByUserId(input.userId)
  if (existing) return existing

  const profile = await input.profileRepo.getOrCreate(input.userId)
  const template = pickCurrentTemplate(profile)
  const theme = template.themePool[0]
  const generated = await input.aiProvider.generateLesson({
    level: profile.currentLevel,
    lessonId: template.templateId,
    grammarFocus: template.grammarFocus,
    writingTask: template.writingTaskType,
    weaknesses: profile.recentWeaknesses,
    theme,
  })

  const lesson = buildLessonInstance({
    userId: input.userId,
    template,
    generated,
    theme,
    generationVersion: 1,
  })
  await input.lessonRepo.insertActiveInstance(lesson)
  await input.profileRepo.updateCurrentTemplate(input.userId, template.templateId, nextThemeRotationState(profile.themeRotationState))
  return lesson
}

export const regenerateTodayLesson = async (input: {
  userId: string
  profileRepo: {
    getOrCreate(userId: string): Promise<UserProfile>
    updateCurrentTemplate(userId: string, templateId: string, themeRotationState: UserProfile['themeRotationState']): Promise<void>
  }
  lessonRepo: {
    findActiveByUserId(userId: string): Promise<LessonInstance | null>
    insertActiveInstance(lesson: LessonInstance): Promise<void>
  }
  aiProvider: AiProvider
}): Promise<LessonInstance> => {
  const profile = await input.profileRepo.getOrCreate(input.userId)
  const existing = await input.lessonRepo.findActiveByUserId(input.userId)
  const template = pickCurrentTemplate(profile)
  const generationVersion = existing ? existing.generationVersion + 1 : 1
  const theme = template.themePool[(generationVersion - 1) % template.themePool.length]
  const generated = await input.aiProvider.generateLesson({
    level: profile.currentLevel,
    lessonId: template.templateId,
    grammarFocus: template.grammarFocus,
    writingTask: template.writingTaskType,
    weaknesses: profile.recentWeaknesses,
    theme,
  })
  const lesson = buildLessonInstance({
    userId: input.userId,
    template,
    generated,
    theme,
    generationVersion,
  })
  await input.lessonRepo.insertActiveInstance(lesson)
  await input.profileRepo.updateCurrentTemplate(input.userId, template.templateId, nextThemeRotationState(profile.themeRotationState))
  return lesson
}
