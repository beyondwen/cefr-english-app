import { lessonBlueprints } from '../domain/cefrCatalog'
import type { CefrLevel, LessonPayload } from '../domain/types'
import type { AiProvider } from '../providers/aiProvider'

const toQuestionText = (question: string | { prompt?: string }): string =>
  typeof question === 'string' ? question : question.prompt ?? JSON.stringify(question)

export const getNextLesson = async (input: {
  userId: string
  level: CefrLevel
  lessonRepo: {
    findByLessonId(userId: string, lessonId: string): Promise<LessonPayload | null>
    insert(userId: string, lesson: LessonPayload): Promise<void>
  }
  aiProvider: AiProvider
}): Promise<LessonPayload> => {
  const blueprint = lessonBlueprints[input.level][0]
  const cached = await input.lessonRepo.findByLessonId(input.userId, blueprint.lessonId)
  if (cached) return cached

  const generated = await input.aiProvider.generateLesson({
    level: input.level,
    lessonId: blueprint.lessonId,
    grammarFocus: blueprint.grammarFocus,
    writingTask: blueprint.writingTask,
  })

  const lesson: LessonPayload = {
    lessonId: blueprint.lessonId,
    level: input.level,
    unitIndex: blueprint.unitIndex,
    grammarFocus: blueprint.grammarFocus,
    readingText: generated.readingText,
    readingQuestions: generated.readingQuestions.map(toQuestionText),
    grammarExplanation: generated.grammarExplanation,
    grammarQuestions: generated.grammarQuestions.map(toQuestionText),
    writingPrompt: generated.writingPrompt,
  }

  await input.lessonRepo.insert(input.userId, lesson)
  return lesson
}
