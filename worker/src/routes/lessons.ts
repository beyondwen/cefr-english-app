import type { CefrLevel } from '../domain/types'
import type { Env } from '../env'
import { json } from '../lib/json'
import { fakeAiProvider } from '../providers/aiProvider'
import { createLessonRepository } from '../repositories/lessonRepository'
import { getNextLesson } from '../services/lessonService'

export const handleNextLesson = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as { userId: string; level: CefrLevel }
  const lesson = await getNextLesson({
    userId: payload.userId,
    level: payload.level,
    lessonRepo: createLessonRepository(env.DB),
    aiProvider: fakeAiProvider,
  })

  return json(lesson)
}
