import type { CefrLevel, ReviewItem, Weakness } from '../domain/types'
import { lessonTemplatesByLevel } from '../domain/lessonTemplates'
import type { Env } from '../env'
import { json } from '../lib/json'
import { createLessonRepository } from '../repositories/lessonRepository'
import { createProgressRepository } from '../repositories/progressRepository'
import { createReviewProgressRepository } from '../repositories/reviewProgressRepository'
import { createUserRepository } from '../repositories/userRepository'
import { completeLesson } from '../services/progressService'

const reviewTaskByWeakness: Record<Weakness, { title: string; task: string; steps: string[] }> = {
  reading: {
    title: '阅读复习',
    task: '重读上一课短文，划出 3 个关键信息并用英文各写 1 句回答。',
    steps: ['先不看译文，回忆短文主旨', '找出 3 个关键信息', '用英文各写 1 句回答'],
  },
  grammar: {
    title: '语法复习',
    task: '重做 3 个目标句型练习，并用订正版写 2 句自己的例句。',
    steps: ['先不看答案，回忆本课目标句型', '写 2 句自己的例句', '对照反馈改掉语法错误'],
  },
  writing: {
    title: '写作复习',
    task: '复看最近一次反馈，改写 1 段短文并检查语法、词汇和连贯性。',
    steps: ['先读原文并找出 1 个主要问题', '不看参考改写，自己重写一版', '对照反馈检查语法、词汇和连贯性'],
  },
}

const buildReviewItems = (
  weaknesses: Weakness[],
  progressByReviewId: Map<string, { status: 'pending' | 'completed'; masteryScore: number }>,
): ReviewItem[] =>
  weaknesses.map((weakness) => {
    const reviewId = `weakness:${weakness}`
    const progress = progressByReviewId.get(reviewId)
    return {
      reviewId,
      skill: weakness,
      ...reviewTaskByWeakness[weakness],
      status: progress?.status ?? 'pending',
      masteryScore: progress?.masteryScore ?? 0,
    }
  })

export const handleSummary = async (request: Request, env: Env): Promise<Response> => {
  const userId = new URL(request.url).searchParams.get('userId') ?? ''
  if (!userId) return json({ error: 'Missing userId' }, 400)
  const user = userId ? await createUserRepository(env.DB).get(userId) : null
  const progress = userId ? await createProgressRepository(env.DB).get(userId) : null
  const reviewProgress = userId ? await createReviewProgressRepository(env.DB).getByUser(userId) : new Map()
  const activeLesson = userId ? await createLessonRepository(env.DB).findActiveByUserId(userId) : null
  const currentLevel = user?.currentLevel ?? progress?.level ?? 'A1'
  const completedCount = progress?.completedLessonIds.length ?? 0
  const totalLessonCount = lessonTemplatesByLevel[currentLevel as CefrLevel]?.length ?? 0
  const progressRatio = totalLessonCount > 0 ? completedCount / totalLessonCount : 0

  return json({
    currentLevel,
    completedCount,
    totalLessonCount,
    progressRatio,
    currentLessonId: activeLesson?.templateId ?? progress?.currentTemplateId ?? null,
    nextLessonId: progress?.currentTemplateId ?? activeLesson?.templateId ?? null,
    todayCompleted: progress?.todayCompleted ?? false,
    recentWeaknesses: user?.recentWeaknesses ?? [],
    reviewItems: buildReviewItems(user?.recentWeaknesses ?? [], reviewProgress),
  })
}

export const handleCompleteReview = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as { userId: string; reviewId: string }
  if (!payload.userId || !payload.reviewId) return json({ error: 'Invalid review request' }, 400)
  return json(await createReviewProgressRepository(env.DB).complete(payload.userId, payload.reviewId))
}

export const handleCompleteLesson = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as { userId: string; level: CefrLevel; lessonId: string }
  if (!payload.userId || !payload.level || !payload.lessonId) return json({ error: 'Invalid progress request' }, 400)
  const result = await completeLesson({
    userId: payload.userId,
    level: payload.level,
    lessonId: payload.lessonId,
    progressRepo: createProgressRepository(env.DB),
  })

  return json(result)
}
