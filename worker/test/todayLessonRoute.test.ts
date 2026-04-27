import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('GET /api/today-lesson', () => {
  it('returns a generated lesson instance', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(new Request('http://localhost/api/today-lesson?userId=u1'))

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toMatchObject({
      lessonInstanceId: 'u1:A1-01:v1',
      templateId: 'A1-01',
      level: 'A1',
      generationVersion: 1,
    })
  })

  it('returns the cached lesson instance for repeat requests', async () => {
    const app = createApp(createFakeEnv())

    await app.fetch(new Request('http://localhost/api/today-lesson?userId=u1'))
    const second = await app.fetch(new Request('http://localhost/api/today-lesson?userId=u1'))

    expect(second.status).toBe(200)
    await expect(second.json()).resolves.toMatchObject({
      lessonInstanceId: 'u1:A1-01:v1',
      generationVersion: 1,
    })
  })
})

describe('POST /api/today-lesson/regenerate', () => {
  it('returns a new version for the current lesson instance', async () => {
    const app = createApp(createFakeEnv())

    await app.fetch(new Request('http://localhost/api/today-lesson?userId=u1'))
    const regenerated = await app.fetch(
      new Request('http://localhost/api/today-lesson/regenerate', {
        method: 'POST',
        body: JSON.stringify({ userId: 'u1' }),
      }),
    )

    expect(regenerated.status).toBe(200)
    await expect(regenerated.json()).resolves.toMatchObject({
      lessonInstanceId: 'u1:A1-01:v2',
      generationVersion: 2,
    })
  })
})
