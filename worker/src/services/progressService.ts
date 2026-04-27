import { lessonTemplatesByLevel } from '../domain/lessonTemplates'
import type { CefrLevel, LessonSubmitResult } from '../domain/types'

export const completeLesson = async (input: {
  userId: string
  level: CefrLevel
  lessonId: string
  progressRepo: {
    get(
      userId: string,
    ): Promise<{ completedLessonIds: string[]; level: string; currentTemplateId: string | null; currentLessonInstanceId: string | null; todayCompleted: boolean } | null>
    upsert(input: {
      userId: string
      level: string
      completedLessonIds: string[]
      currentTemplateId: string | null
      currentLessonInstanceId: string | null
      todayCompleted: boolean
    }): Promise<void>
  }
}) => {
  const current = (await input.progressRepo.get(input.userId)) ?? {
    completedLessonIds: [],
    level: input.level,
    currentTemplateId: null,
    currentLessonInstanceId: null,
    todayCompleted: false,
  }
  const completedLessonIds = [...new Set([...current.completedLessonIds, input.lessonId])]
  const allIds = lessonTemplatesByLevel[input.level].map((item) => item.templateId)
  const nextLessonId = allIds.find((item) => !completedLessonIds.includes(item)) ?? null
  await input.progressRepo.upsert({
    userId: input.userId,
    level: input.level,
    completedLessonIds,
    currentTemplateId: nextLessonId,
    currentLessonInstanceId: null,
    todayCompleted: true,
  })

  return { completedLessonIds, nextLessonId }
}

export const submitLesson = async (input: {
  userId: string
  lessonInstanceId: string
  readingAnswers: string[]
  grammarAnswers: string[]
  lessonRepo: {
    findByInstanceId(lessonInstanceId: string): Promise<{ templateId: string; level: CefrLevel; writingPrompt: string } | null>
    markCompleted(lessonInstanceId: string, completedAt: string): Promise<void>
  }
  progressRepo: {
    get(
      userId: string,
    ): Promise<{ completedLessonIds: string[]; level: string; currentTemplateId: string | null; currentLessonInstanceId: string | null; todayCompleted: boolean } | null>
    upsert(input: {
      userId: string
      level: string
      completedLessonIds: string[]
      currentTemplateId: string | null
      currentLessonInstanceId: string | null
      todayCompleted: boolean
    }): Promise<void>
  }
  reviewResult: {
    ruleChecks: { notBlank: boolean; minSentencesOk: boolean; onTopicLikely: boolean }
  }
}): Promise<LessonSubmitResult> => {
  const lesson = await input.lessonRepo.findByInstanceId(input.lessonInstanceId)
  if (!lesson) {
    return {
      completed: false,
      currentLessonId: '',
      nextLessonId: null,
      todayCompleted: false,
      missingRequirements: ['lesson_not_found'],
      ruleChecks: input.reviewResult.ruleChecks,
    }
  }

  const template = lessonTemplatesByLevel[lesson.level].find((item) => item.templateId === lesson.templateId)
  const missingRequirements: string[] = []
  if (input.readingAnswers.length < (template?.questionCounts.reading ?? 0)) missingRequirements.push('reading_incomplete')
  if (input.grammarAnswers.length < (template?.questionCounts.grammar ?? 0)) missingRequirements.push('grammar_incomplete')
  if (!input.reviewResult.ruleChecks.notBlank) missingRequirements.push('writing_blank')
  if (!input.reviewResult.ruleChecks.minSentencesOk) missingRequirements.push('writing_min_sentences')

  if (missingRequirements.length > 0) {
    return {
      completed: false,
      currentLessonId: lesson.templateId,
      nextLessonId: lesson.templateId,
      todayCompleted: false,
      missingRequirements,
      ruleChecks: input.reviewResult.ruleChecks,
    }
  }

  await input.lessonRepo.markCompleted(input.lessonInstanceId, new Date().toISOString())
  const progressResult = await completeLesson({
    userId: input.userId,
    level: lesson.level,
    lessonId: lesson.templateId,
    progressRepo: input.progressRepo,
  })

  return {
    completed: true,
    currentLessonId: lesson.templateId,
    nextLessonId: progressResult.nextLessonId,
    todayCompleted: true,
    missingRequirements: [],
    ruleChecks: input.reviewResult.ruleChecks,
  }
}
