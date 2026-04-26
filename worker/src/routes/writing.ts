import type { Env } from '../env'
import { json } from '../lib/json'
import { fakeAiProvider } from '../providers/aiProvider'
import { createWritingReviewRepository } from '../repositories/writingReviewRepository'
import { reviewWriting } from '../services/writingReviewService'

export const handleWritingReview = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as {
    userId: string
    lessonId: string
    level: string
    prompt: string
    submission: string
  }

  const result = await reviewWriting({
    level: payload.level,
    prompt: payload.prompt,
    submission: payload.submission,
    aiProvider: fakeAiProvider,
  })

  await createWritingReviewRepository(env.DB).insert(
    payload.userId,
    payload.lessonId,
    payload.prompt,
    payload.submission,
    result.feedback,
  )

  return json(result)
}
