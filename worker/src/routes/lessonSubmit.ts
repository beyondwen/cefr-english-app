import type { Env } from '../env'
import { json } from '../lib/json'
import { createAiProvider } from '../providers/aiProvider'
import { createLessonRepository } from '../repositories/lessonRepository'
import { createProgressRepository } from '../repositories/progressRepository'
import { createUserRepository } from '../repositories/userRepository'
import { createWritingReviewRepository } from '../repositories/writingReviewRepository'
import { submitLesson } from '../services/progressService'
import { reviewWriting } from '../services/writingReviewService'
import type { Weakness } from '../domain/types'

const weaknessFromMissingRequirements = (missingRequirements: string[]): Weakness[] => {
  const weaknesses: Weakness[] = []
  if (missingRequirements.some((item) => item.startsWith('reading_'))) weaknesses.push('reading')
  if (missingRequirements.some((item) => item.startsWith('grammar_'))) weaknesses.push('grammar')
  if (missingRequirements.some((item) => item.startsWith('writing_'))) weaknesses.push('writing')
  return weaknesses
}

export const handleLessonSubmit = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as {
    userId: string
    lessonInstanceId: string
    prompt: string
    level: string
    readingAnswers: string[]
    grammarAnswers: string[]
    writingSubmission: string
    writingRevision?: string
  }
  if (
    !payload.userId ||
    !payload.lessonInstanceId ||
    !payload.level ||
    !Array.isArray(payload.readingAnswers) ||
    !Array.isArray(payload.grammarAnswers) ||
    typeof payload.writingSubmission !== 'string'
  ) {
    return json({ error: 'Invalid lesson submission' }, 400)
  }

  const reviewResult = await reviewWriting({
    level: payload.level,
    prompt: payload.prompt,
    submission: payload.writingSubmission,
    aiProvider: createAiProvider(env),
  })

  await createWritingReviewRepository(env.DB).insertSubmission(
    payload.lessonInstanceId,
    payload.userId,
    payload.readingAnswers,
    payload.grammarAnswers,
    payload.writingSubmission,
    reviewResult.feedback,
  )

  const result = await submitLesson({
    userId: payload.userId,
    lessonInstanceId: payload.lessonInstanceId,
    readingAnswers: payload.readingAnswers,
    grammarAnswers: payload.grammarAnswers,
    writingRevision: payload.writingRevision,
    lessonRepo: createLessonRepository(env.DB),
    progressRepo: createProgressRepository(env.DB),
    reviewResult,
  })
  await createUserRepository(env.DB).mergeWeaknesses(payload.userId, weaknessFromMissingRequirements(result.missingRequirements))

  return json({
    ...result,
    feedback: reviewResult.feedback,
  })
}
