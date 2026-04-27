import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('worker e2e flow', () => {
  it('supports placement -> today lesson -> submit -> summary', async () => {
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

    const lesson = await app.fetch(new Request('http://localhost/api/today-lesson?userId=u1'))
    const lessonJson = (await lesson.json()) as {
      lessonInstanceId: string
      level: 'A2'
      writingPrompt: string
    }

    const submit = await app.fetch(
      new Request('http://localhost/api/lesson/submit', {
        method: 'POST',
        body: JSON.stringify({
          userId: 'u1',
          lessonInstanceId: lessonJson.lessonInstanceId,
          level: lessonJson.level,
          prompt: lessonJson.writingPrompt,
          readingAnswers: ['a', 'b', 'c', 'd'],
          grammarAnswers: ['a', 'b', 'c', 'd'],
          writingSubmission: 'Last year I visited my aunt. We cooked dinner together.',
        }),
      }),
    )

    const summary = await app.fetch(new Request('http://localhost/api/me/summary?userId=u1'))

    expect(placement.status).toBe(200)
    expect(lesson.status).toBe(200)
    expect(submit.status).toBe(200)
    expect(summary.status).toBe(200)
  })
})
