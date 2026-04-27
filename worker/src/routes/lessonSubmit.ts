import type { Env } from '../env'
import { json } from '../lib/json'
import { fakeAiProvider } from '../providers/aiProvider'
import { createLessonRepository } from '../repositories/lessonRepository'
import { createProgressRepository } from '../repositories/progressRepository'
import { createWritingReviewRepository } from '../repositories/writingReviewRepository'
import { submitLesson } from '../services/progressService'
import { reviewWriting } from '../services/writingReviewService'

export const handleLessonSubmit = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as {
    userId: string
    lessonInstanceId: string
    prompt: string
    level: string
    readingAnswers: string[]
    grammarAnswers: string[]
    writingSubmission: string
  }

  const reviewResult = await reviewWriting({
    level: payload.level,
    prompt: payload.prompt,
    submission: payload.writingSubmission,
    aiProvider: fakeAiProvider,
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
    lessonRepo: createLessonRepository(env.DB),
    progressRepo: createProgressRepository(env.DB),
    reviewResult,
  })

  return json({
    ...result,
    feedback: reviewResult.feedback,
  })
}
