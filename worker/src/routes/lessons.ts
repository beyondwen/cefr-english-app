import type { CefrLevel } from '../domain/types'
import type { Env } from '../env'
import { json } from '../lib/json'
import { fakeAiProvider } from '../providers/aiProvider'
import { createLessonRepository } from '../repositories/lessonRepository'
import { createUserRepository } from '../repositories/userRepository'
import { getNextLesson } from '../services/lessonService'
import { getTodayLesson, regenerateTodayLesson } from '../services/todayLessonService'

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

export const handleTodayLesson = async (request: Request, env: Env): Promise<Response> => {
  const userId = new URL(request.url).searchParams.get('userId') ?? ''
  const lesson = await getTodayLesson({
    userId,
    profileRepo: createUserRepository(env.DB),
    lessonRepo: createLessonRepository(env.DB),
    aiProvider: fakeAiProvider,
  })
  return json(lesson)
}

export const handleRegenerateTodayLesson = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as { userId: string }
  const lesson = await regenerateTodayLesson({
    userId: payload.userId,
    profileRepo: createUserRepository(env.DB),
    lessonRepo: createLessonRepository(env.DB),
    aiProvider: fakeAiProvider,
  })
  return json(lesson)
}
