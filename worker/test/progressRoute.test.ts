import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('GET /api/me/summary', () => {
  it('returns empty progress summary for new user', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(new Request('http://localhost/api/me/summary?userId=u1'))

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toEqual({
      currentLevel: 'A1',
      completedCount: 0,
      currentLessonId: null,
      nextLessonId: null,
      todayCompleted: false,
      recentWeaknesses: [],
    })
  })
})

describe('POST /api/progress/complete', () => {
  it('returns completed lessons and next lesson id', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(
      new Request('http://localhost/api/progress/complete', {
        method: 'POST',
        body: JSON.stringify({ userId: 'u1', level: 'A1', lessonId: 'A1-01' }),
      }),
    )

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toEqual({
      completedLessonIds: ['A1-01'],
      nextLessonId: 'A1-02',
    })
  })
})
