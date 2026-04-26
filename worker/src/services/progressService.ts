import { lessonBlueprints } from '../domain/cefrCatalog'
import type { CefrLevel } from '../domain/types'

export const completeLesson = async (input: {
  userId: string
  level: CefrLevel
  lessonId: string
  progressRepo: {
    get(userId: string): Promise<{ completedLessonIds: string[]; level: string } | null>
    upsert(userId: string, level: string, completedLessonIds: string[]): Promise<void>
  }
}) => {
  const current = (await input.progressRepo.get(input.userId)) ?? { completedLessonIds: [], level: input.level }
  const completedLessonIds = [...new Set([...current.completedLessonIds, input.lessonId])]
  await input.progressRepo.upsert(input.userId, input.level, completedLessonIds)

  const allIds = lessonBlueprints[input.level].map((item) => item.lessonId)
  const nextLessonId = allIds.find((item) => !completedLessonIds.includes(item)) ?? null
  return { completedLessonIds, nextLessonId }
}
