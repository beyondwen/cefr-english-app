import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('worker e2e flow', () => {
  it('supports placement -> lesson -> writing -> complete', async () => {
    const app = createApp(createFakeEnv())

    const placement = await app.fetch(
      new Request('http://localhost/api/placement/assess', {
        method: 'POST',
        body: JSON.stringify({
          userId: 'u1',
          answers: [
            { skill: 'reading', correct: 4, total: 5 },
            { skill: 'grammar', correct: 4, total: 5 },
          ],
          writingWordCount: 60,
        }),
      }),
    )

    const lesson = await app.fetch(
      new Request('http://localhost/api/lessons/next', {
        method: 'POST',
        body: JSON.stringify({ userId: 'u1', level: 'A2' }),
      }),
    )

    const writing = await app.fetch(
      new Request('http://localhost/api/writing/review', {
        method: 'POST',
        body: JSON.stringify({
          userId: 'u1',
          lessonId: 'A2-01',
          level: 'A2',
          prompt: 'Write about a past experience.',
          submission: 'Last year I visited my aunt and we cooked dinner together.',
        }),
      }),
    )

    const progress = await app.fetch(
      new Request('http://localhost/api/progress/complete', {
        method: 'POST',
        body: JSON.stringify({ userId: 'u1', level: 'A2', lessonId: 'A2-01' }),
      }),
    )

    expect(placement.status).toBe(200)
    expect(lesson.status).toBe(200)
    expect(writing.status).toBe(200)
    expect(progress.status).toBe(200)
  })
})
