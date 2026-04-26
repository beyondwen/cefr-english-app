import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('POST /api/lessons/next', () => {
  it('returns a lesson payload', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(
      new Request('http://localhost/api/lessons/next', {
        method: 'POST',
        body: JSON.stringify({ userId: 'u1', level: 'A1' }),
      }),
    )

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toMatchObject({
      lessonId: 'A1-01',
      level: 'A1',
    })
  })
})
