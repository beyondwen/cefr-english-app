import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('POST /api/placement/assess', () => {
  it('returns level and weaknesses', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(
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

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toEqual({ level: 'A2', weaknesses: ['grammar'] })
  })
})

describe('GET /api/placement/test', () => {
  it('returns a placement test', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(new Request('http://localhost/api/placement/test'))

    expect(response.status).toBe(200)
    const payload = (await response.json()) as {
      readingPassage?: string
      readingQuestions?: unknown[]
      grammarQuestions?: unknown[]
      writingPrompt?: string
      minWritingWords?: number
    }
    expect(payload.readingPassage).toBeTruthy()
    expect(payload.readingQuestions).toHaveLength(5)
    expect(payload.grammarQuestions).toHaveLength(5)
    expect(payload.writingPrompt).toBeTruthy()
    expect(payload.minWritingWords).toBeGreaterThanOrEqual(20)
  })
})
