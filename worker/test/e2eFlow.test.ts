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
            { skill: 'reading', correct: 3, total: 5 },
            { skill: 'grammar', correct: 3, total: 5 },
          ],
          writingWordCount: 70,
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
          readingAnswers: ['daily life', 'hello', 'c', 'd'],
          grammarAnswers: ['am', 'I am a student.', 'c', 'd'],
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

  it('turns incorrect answers into specific review items', async () => {
    const app = createApp(createFakeEnv())
    await app.fetch(
      new Request('http://localhost/api/placement/assess', {
        method: 'POST',
        body: JSON.stringify({
          userId: 'u1',
          answers: [
            { skill: 'reading', correct: 3, total: 5 },
            { skill: 'grammar', correct: 3, total: 5 },
          ],
          writingWordCount: 70,
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
          readingAnswers: ['wrong reading', 'hello', 'c', 'd'],
          grammarAnswers: ['am', 'wrong grammar', 'c', 'd'],
          writingSubmission:
            'I studied English today because I want to speak with more people. I wrote a short paragraph and checked my answers carefully. I will practice again tomorrow so I can remember the new words and use them in real conversations with my classmates. I also listened to a short dialogue after dinner and repeated the useful sentences several times. This habit helps me feel more confident when I answer questions in class.',
        }),
      }),
    )
    const submitJson = (await submit.json()) as { completed: boolean; missingRequirements: string[] }
    const summary = await app.fetch(new Request('http://localhost/api/me/summary?userId=u1'))

    expect(submitJson.completed).toBe(false)
    expect(submitJson.missingRequirements).toEqual(['reading_incorrect', 'grammar_incorrect'])
    const summaryJson = await summary.json()
    expect(summaryJson.recentWeaknesses).toEqual(['reading', 'grammar'])
    expect(summaryJson.reviewItems).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          reviewId: 'answer:u1:A2-01:v1:reading:A2-01-reading-1',
          skill: 'reading',
          title: '订正阅读错题',
        }),
        expect.objectContaining({
          reviewId: 'answer:u1:A2-01:v1:grammar:A2-01-grammar-2',
          skill: 'grammar',
          title: '订正语法错题',
        }),
      ]),
    )
  })

  it('turns structured writing feedback into due review items and reschedules them after completion', async () => {
    const app = createApp(createFakeEnv())
    await app.fetch(
      new Request('http://localhost/api/placement/assess', {
        method: 'POST',
        body: JSON.stringify({
          userId: 'u1',
          answers: [
            { skill: 'reading', correct: 3, total: 5 },
            { skill: 'grammar', correct: 3, total: 5 },
          ],
          writingWordCount: 70,
        }),
      }),
    )
    const lesson = await app.fetch(new Request('http://localhost/api/today-lesson?userId=u1'))
    const lessonJson = (await lesson.json()) as {
      lessonInstanceId: string
      level: 'A2'
      writingPrompt: string
    }

    await app.fetch(
      new Request('http://localhost/api/lesson/submit', {
        method: 'POST',
        body: JSON.stringify({
          userId: 'u1',
          lessonInstanceId: lessonJson.lessonInstanceId,
          level: lessonJson.level,
          prompt: lessonJson.writingPrompt,
          readingAnswers: ['daily life', 'hello', 'c', 'd'],
          grammarAnswers: ['am', 'I am a student.', 'c', 'd'],
          writingSubmission:
            'I wake up early and I prepare my school bag before breakfast. I walk to school with my friend because the weather is usually nice. After class, I review new words and write two more sentences so I can improve my English every day.',
        }),
      }),
    )

    const firstSummary = await app.fetch(new Request('http://localhost/api/me/summary?userId=u1'))
    const firstSummaryJson = await firstSummary.json()
    expect(firstSummaryJson.reviewItems).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          reviewId: 'writing:u1:A2-01:v1:0',
          skill: 'writing',
          title: '订正写作问题：connector',
          dueDate: expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/),
        }),
      ]),
    )

    await app.fetch(
      new Request('http://localhost/api/review/complete', {
        method: 'POST',
        body: JSON.stringify({ userId: 'u1', reviewId: 'writing:u1:A2-01:v1:0' }),
      }),
    )

    const secondSummary = await app.fetch(new Request('http://localhost/api/me/summary?userId=u1'))
    const secondSummaryJson = await secondSummary.json()
    expect(secondSummaryJson.reviewItems).not.toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          reviewId: 'writing:u1:A2-01:v1:0',
        }),
      ]),
    )
  })
})
