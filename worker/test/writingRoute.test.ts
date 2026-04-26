import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('POST /api/writing/review', () => {
  it('returns writing review payload', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(
      new Request('http://localhost/api/writing/review', {
        method: 'POST',
        body: JSON.stringify({
          userId: 'u1',
          lessonId: 'A1-01',
          level: 'A1',
          prompt: 'Write about your daily routine.',
          submission: 'I wake up at 7 and go to school every day.',
        }),
      }),
    )

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toMatchObject({
      ruleChecks: { wordCountOk: true },
    })
  })
})
