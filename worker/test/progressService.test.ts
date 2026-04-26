import { describe, expect, it } from 'vitest'
import { completeLesson } from '../src/services/progressService'

describe('completeLesson', () => {
  it('returns next lesson id after completion', async () => {
    const result = await completeLesson({
      userId: 'u1',
      level: 'A1',
      lessonId: 'A1-01',
      progressRepo: {
        get: async () => ({ completedLessonIds: [] }),
        upsert: async () => undefined,
      },
    })

    expect(result.completedLessonIds).toEqual(['A1-01'])
    expect(result.nextLessonId).toBe('A1-02')
  })
})
