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

  it('regenerates and stores a syllabus for the selected CEFR level', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(
      new Request('http://localhost/api/syllabus/regenerate', {
        method: 'POST',
        body: JSON.stringify({ level: 'B2' }),
      }),
    )

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toMatchObject({
      level: 'B2',
      title: 'B2 课程大纲',
      modules: [{ title: '核心能力入门' }],
    })

    const cached = await app.fetch(new Request('http://localhost/api/syllabus?level=B2'))
    await expect(cached.json()).resolves.toMatchObject({
      level: 'B2',
      title: 'B2 课程大纲',
    })
  })
})
