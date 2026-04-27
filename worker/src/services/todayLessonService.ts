import { lessonTemplatesByLevel } from '../domain/lessonTemplates'
import type { LessonInstance, LessonQuestion, LessonTemplate, UserProfile } from '../domain/types'
import type { AiProvider } from '../providers/aiProvider'

const createLessonQuestions = (prompts: string[], prefix: string): LessonQuestion[] =>
  prompts.map((prompt, index) => {
    const text = typeof prompt === 'string' ? prompt : JSON.stringify(prompt)
    return {
      questionId: `${prefix}-${index + 1}`,
      prompt: text,
      choices: [],
      answer: '',
    }
  })

const nextThemeRotationState = (current: UserProfile['themeRotationState']): UserProfile['themeRotationState'] =>
  current === 'life' ? 'expression' : 'life'

const createLessonInstanceId = (userId: string, templateId: string, version: number): string => `${userId}:${templateId}:v${version}`

const isPlaceholderLesson = (lesson: LessonInstance): boolean =>
  lesson.readingText.startsWith('Sample reading for ') ||
  lesson.grammarExplanation.startsWith('Focus on ') ||
  lesson.writingPrompt.startsWith('Write a ') ||
  !lesson.objectives?.length ||
  !lesson.vocabulary?.length ||
  !lesson.keySentences?.length ||
  !lesson.dialogue?.length

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
  objectives: input.generated.objectives,
  warmupQuestions: input.generated.warmupQuestions,
  vocabulary: input.generated.vocabulary,
  keySentences: input.generated.keySentences,
  dialogue: input.generated.dialogue,
  speakingPractice: input.generated.speakingPractice,
  listeningPractice: input.generated.listeningPractice,
  reviewTasks: input.generated.reviewTasks,
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

const buildSyllabusLessonTemplate = (input: {
  level: LessonTemplate['level']
  moduleIndex: number
  moduleTitle: string
  lessons: string[]
}): LessonTemplate => ({
  templateId: `${input.level}-module-${String(input.moduleIndex + 1).padStart(2, '0')}`,
  level: input.level,
  sequence: input.moduleIndex + 1,
  grammarFocus: input.lessons[0] ?? input.moduleTitle,
  readingTaskType: 'reading_mcq',
  writingTaskType: 'short_paragraph',
  themePool: [input.moduleTitle, ...input.lessons].slice(0, 4),
  targetWordRange: input.level === 'A1' ? { min: 60, max: 100 } : { min: 100, max: 160 },
  questionCounts: { reading: 3, grammar: 3 },
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

export const getSyllabusModuleLesson = async (input: {
  userId: string
  level: LessonTemplate['level']
  moduleIndex: number
  module: {
    title: string
    goal: string
    lessons: string[]
  }
  lessonRepo: {
    findByInstanceId(lessonInstanceId: string): Promise<LessonInstance | null>
    insertActiveInstance(lesson: LessonInstance): Promise<void>
  }
  aiProvider: AiProvider
}): Promise<LessonInstance> => {
  const template = buildSyllabusLessonTemplate({
    level: input.level,
    moduleIndex: input.moduleIndex,
    moduleTitle: input.module.title,
    lessons: input.module.lessons,
  })
  const lessonInstanceId = createLessonInstanceId(input.userId, template.templateId, 1)
  const existing = await input.lessonRepo.findByInstanceId(lessonInstanceId)
  if (existing && !isPlaceholderLesson(existing)) return existing

  const generated = await input.aiProvider.generateLesson({
    level: input.level,
    lessonId: template.templateId,
    grammarFocus: `${input.module.title}: ${input.module.lessons.join(' / ')}`,
    writingTask: input.module.goal,
    weaknesses: [],
    theme: input.module.title,
  })

  const lesson = buildLessonInstance({
    userId: input.userId,
    template,
    generated,
    theme: input.module.title,
    generationVersion: 1,
  })
  await input.lessonRepo.insertActiveInstance(lesson)
  return lesson
}
