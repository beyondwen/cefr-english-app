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
      totalLessonCount: 10,
      progressRatio: 0,
      currentLessonId: null,
      nextLessonId: null,
      todayCompleted: false,
      recentWeaknesses: [],
      reviewItems: [],
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
      totalLessonCount: 10,
      progressRatio: 0.1,
    })
  })

  it('returns actionable review items from recent weaknesses', async () => {
    const app = createApp(createFakeEnv())
    const placementResponse = await app.fetch(
      new Request('http://localhost/api/placement/assess', {
        method: 'POST',
        body: JSON.stringify({
          userId: 'u1',
          answers: [
            { skill: 'reading', correct: 4, total: 5 },
            { skill: 'grammar', correct: 2, total: 5 },
          ],
          writingWordCount: 70,
        }),
      }),
    )
    expect(placementResponse.status).toBe(200)
    await expect(placementResponse.json()).resolves.toEqual({ level: 'A2', weaknesses: ['grammar'] })

    const response = await app.fetch(new Request('http://localhost/api/me/summary?userId=u1'))

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toMatchObject({
      recentWeaknesses: ['grammar'],
      reviewItems: [
        {
          reviewId: 'weakness:grammar',
          skill: 'grammar',
          title: '语法复习',
          task: '重做 3 个目标句型练习，并用订正版写 2 句自己的例句。',
          steps: ['先不看答案，回忆本课目标句型', '写 2 句自己的例句', '对照反馈改掉语法错误'],
          status: 'pending',
          masteryScore: 0,
        },
      ],
    })
  })

  it('marks a review item complete and raises mastery score', async () => {
    const app = createApp(createFakeEnv())
    await app.fetch(
      new Request('http://localhost/api/placement/assess', {
        method: 'POST',
        body: JSON.stringify({
          userId: 'u1',
          answers: [
            { skill: 'reading', correct: 4, total: 5 },
            { skill: 'grammar', correct: 2, total: 5 },
          ],
          writingWordCount: 70,
        }),
      }),
    )

    const complete = await app.fetch(
      new Request('http://localhost/api/review/complete', {
        method: 'POST',
        body: JSON.stringify({ userId: 'u1', reviewId: 'weakness:grammar' }),
      }),
    )
    const summary = await app.fetch(new Request('http://localhost/api/me/summary?userId=u1'))

    expect(complete.status).toBe(200)
    await expect(complete.json()).resolves.toEqual({
      reviewId: 'weakness:grammar',
      status: 'completed',
      masteryScore: 1,
    })
    await expect(summary.json()).resolves.toMatchObject({
      reviewItems: [
        {
          reviewId: 'weakness:grammar',
          status: 'completed',
          masteryScore: 1,
        },
      ],
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
