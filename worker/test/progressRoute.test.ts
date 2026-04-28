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
      totalLessonCount: 2,
      progressRatio: 0,
      currentLessonId: null,
      nextLessonId: null,
      todayCompleted: false,
      recentWeaknesses: [],
    })
  })
})

describe('GET /api/me/summary after progress', () => {
  it('returns real progress ratio from completed lessons and level total', async () => {
    const app = createApp(createFakeEnv())
    await app.fetch(
      new Request('http://localhost/api/progress/complete', {
        method: 'POST',
        body: JSON.stringify({ userId: 'u1', level: 'A1', lessonId: 'A1-01' }),
      }),
    )

    const response = await app.fetch(new Request('http://localhost/api/me/summary?userId=u1'))

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toMatchObject({
      completedCount: 1,
      totalLessonCount: 2,
      progressRatio: 0.5,
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
