import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('GET /api/syllabus', () => {
  it('returns the syllabus for the selected CEFR level', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(new Request('http://localhost/api/syllabus?level=A1'))

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toMatchObject({
      level: 'A1',
      title: 'A1 入门基础',
      modules: [{ title: '自我介绍' }],
    })
  })

  it('rejects unsupported levels', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(new Request('http://localhost/api/syllabus?level=Z9'))

    expect(response.status).toBe(400)
  })
})
