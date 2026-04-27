import type { CefrLevel } from '../domain/types'
import type { Env } from '../env'
import { json } from '../lib/json'
import { createLessonRepository } from '../repositories/lessonRepository'
import { createProgressRepository } from '../repositories/progressRepository'
import { createUserRepository } from '../repositories/userRepository'
import { completeLesson } from '../services/progressService'

export const handleSummary = async (request: Request, env: Env): Promise<Response> => {
  const userId = new URL(request.url).searchParams.get('userId') ?? ''
  const user = userId ? await createUserRepository(env.DB).get(userId) : null
  const progress = userId ? await createProgressRepository(env.DB).get(userId) : null
  const activeLesson = userId ? await createLessonRepository(env.DB).findActiveByUserId(userId) : null

  return json({
    currentLevel: user?.currentLevel ?? progress?.level ?? 'A1',
    completedCount: progress?.completedLessonIds.length ?? 0,
    currentLessonId: activeLesson?.templateId ?? progress?.currentTemplateId ?? null,
    nextLessonId: progress?.currentTemplateId ?? activeLesson?.templateId ?? null,
    todayCompleted: progress?.todayCompleted ?? false,
    recentWeaknesses: user?.recentWeaknesses ?? [],
  })
}

export const handleCompleteLesson = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as { userId: string; level: CefrLevel; lessonId: string }
  const result = await completeLesson({
    userId: payload.userId,
    level: payload.level,
    lessonId: payload.lessonId,
    progressRepo: createProgressRepository(env.DB),
  })

  return json(result)
}
