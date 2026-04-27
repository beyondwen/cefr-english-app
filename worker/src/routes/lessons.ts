import type { CefrLevel } from '../domain/types'
import type { Env } from '../env'
import { json } from '../lib/json'
import { createAiProvider } from '../providers/aiProvider'
import { createLessonRepository } from '../repositories/lessonRepository'
import { SyllabusRepository } from '../repositories/syllabusRepository'
import { createUserRepository } from '../repositories/userRepository'
import { getNextLesson } from '../services/lessonService'
import { getSyllabusModuleLesson, getTodayLesson, regenerateTodayLesson } from '../services/todayLessonService'

export const handleNextLesson = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as { userId: string; level: CefrLevel }
  const aiProvider = createAiProvider(env)
  const lesson = await getNextLesson({
    userId: payload.userId,
    level: payload.level,
    lessonRepo: createLessonRepository(env.DB),
    aiProvider,
  })

  return json(lesson)
}

export const handleTodayLesson = async (request: Request, env: Env): Promise<Response> => {
  const userId = new URL(request.url).searchParams.get('userId') ?? ''
  const aiProvider = createAiProvider(env)
  const lesson = await getTodayLesson({
    userId,
    profileRepo: createUserRepository(env.DB),
    lessonRepo: createLessonRepository(env.DB),
    aiProvider,
  })
  return json(lesson)
}

export const handleSyllabusModuleLesson = async (request: Request, env: Env): Promise<Response> => {
  const url = new URL(request.url)
  const userId = url.searchParams.get('userId') ?? ''
  const level = url.searchParams.get('level') as CefrLevel | null
  const moduleIndex = Number(url.searchParams.get('moduleIndex') ?? '-1')

  if (!userId || !level || !Number.isInteger(moduleIndex) || moduleIndex < 0) {
    return json({ error: 'Invalid lesson request' }, 400)
  }

  const syllabus = await new SyllabusRepository(env.DB).findByLevel(level)
  const module = syllabus?.modules[moduleIndex]
  if (!module) {
    return json({ error: 'Syllabus module not found' }, 404)
  }

  const lesson = await getSyllabusModuleLesson({
    userId,
    level,
    moduleIndex,
    module,
    lessonRepo: createLessonRepository(env.DB),
    aiProvider: createAiProvider(env),
  })

  return json(lesson)
}

export const handleRegenerateTodayLesson = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as { userId: string }
  const aiProvider = createAiProvider(env)
  const lesson = await regenerateTodayLesson({
    userId: payload.userId,
    profileRepo: createUserRepository(env.DB),
    lessonRepo: createLessonRepository(env.DB),
    aiProvider,
  })
  return json(lesson)
}
