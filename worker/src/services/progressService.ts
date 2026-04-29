import { lessonTemplatesByLevel } from '../domain/lessonTemplates'
import type { CefrLevel, LessonAnswerFeedback, LessonQuestion, LessonSubmitResult } from '../domain/types'

export const currentDateKey = (date = new Date()): string => date.toISOString().slice(0, 10)

const allAnswered = (answers: string[], expectedCount: number): boolean =>
  answers.length >= expectedCount && answers.slice(0, expectedCount).every((answer) => answer.trim().length > 0)

const normalizeAnswer = (answer: string): string => answer.trim().toLowerCase().replace(/\s+/g, ' ')

const gradeAnswers = (questions: LessonQuestion[] | undefined, answers: string[]): LessonAnswerFeedback[] => {
  if (!questions?.length) return []
  return questions.flatMap((question, index) => {
    const expectedAnswer = question.answer.trim()
    if (!expectedAnswer) return []
    const submittedAnswer = answers[index] ?? ''
    const correct = normalizeAnswer(submittedAnswer) === normalizeAnswer(expectedAnswer)
    if (correct) return []
    return [
      {
        questionId: question.questionId,
        prompt: question.prompt,
        expectedAnswer,
        submittedAnswer,
        correct,
      },
    ]
  })
}

export const completeLesson = async (input: {
  userId: string
  level: CefrLevel
  lessonId: string
  progressRepo: {
    get(
      userId: string,
    ): Promise<{
      completedLessonIds: string[]
      level: string
      currentTemplateId: string | null
      currentLessonInstanceId: string | null
      todayCompleted: boolean
      lastCompletedDate: string | null
    } | null>
    upsert(input: {
      userId: string
      level: string
      completedLessonIds: string[]
      currentTemplateId: string | null
      currentLessonInstanceId: string | null
      lastCompletedDate: string | null
    }): Promise<void>
  }
}) => {
  const current = (await input.progressRepo.get(input.userId)) ?? {
    completedLessonIds: [],
    level: input.level,
    currentTemplateId: null,
    currentLessonInstanceId: null,
    todayCompleted: false,
    lastCompletedDate: null,
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
    lastCompletedDate: currentDateKey(),
  })

  return { completedLessonIds, nextLessonId }
}

export const submitLesson = async (input: {
  userId: string
  lessonInstanceId: string
  readingAnswers: string[]
  grammarAnswers: string[]
  writingRevision?: string | null
  lessonRepo: {
    findByInstanceId(lessonInstanceId: string): Promise<{
      templateId: string
      level: CefrLevel
      writingPrompt: string
      readingQuestions?: LessonQuestion[]
      grammarQuestions?: LessonQuestion[]
    } | null>
    markCompleted(lessonInstanceId: string, completedAt: string): Promise<void>
  }
  progressRepo: {
    get(
      userId: string,
    ): Promise<{
      completedLessonIds: string[]
      level: string
      currentTemplateId: string | null
      currentLessonInstanceId: string | null
      todayCompleted: boolean
      lastCompletedDate: string | null
    } | null>
    upsert(input: {
      userId: string
      level: string
      completedLessonIds: string[]
      currentTemplateId: string | null
      currentLessonInstanceId: string | null
      lastCompletedDate: string | null
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
      revisionRequired: false,
    }
  }

  const template = lessonTemplatesByLevel[lesson.level].find((item) => item.templateId === lesson.templateId)
  const missingRequirements: string[] = []
  const answerFeedback = {
    reading: gradeAnswers(lesson.readingQuestions, input.readingAnswers),
    grammar: gradeAnswers(lesson.grammarQuestions, input.grammarAnswers),
  }
  if (!allAnswered(input.readingAnswers, template?.questionCounts.reading ?? 0)) missingRequirements.push('reading_incomplete')
  if (!allAnswered(input.grammarAnswers, template?.questionCounts.grammar ?? 0)) missingRequirements.push('grammar_incomplete')
  if (answerFeedback.reading.length > 0) missingRequirements.push('reading_incorrect')
  if (answerFeedback.grammar.length > 0) missingRequirements.push('grammar_incorrect')
  if (!input.reviewResult.ruleChecks.notBlank) missingRequirements.push('writing_blank')
  if (!input.reviewResult.ruleChecks.minSentencesOk) missingRequirements.push('writing_min_sentences')
  if (input.writingRevision != null && input.writingRevision.trim().length === 0) {
    missingRequirements.push('writing_revision_required')
  }

  if (missingRequirements.length > 0) {
    return {
      completed: false,
      currentLessonId: lesson.templateId,
      nextLessonId: lesson.templateId,
      todayCompleted: false,
      missingRequirements,
      ruleChecks: input.reviewResult.ruleChecks,
      revisionRequired: missingRequirements.includes('writing_revision_required'),
      answerFeedback,
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
    revisionRequired: false,
    answerFeedback,
  }
}
