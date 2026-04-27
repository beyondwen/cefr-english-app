import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('GET /api/syllabus-lesson', () => {
  it('creates a lesson for the selected syllabus module', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(
      new Request('http://localhost/api/syllabus-lesson?userId=user-1&level=A1&moduleIndex=0'),
    )

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toMatchObject({
      lessonInstanceId: 'user-1:A1-module-01:v1',
      templateId: 'A1-module-01',
      level: 'A1',
      theme: '自我介绍',
    })
  })
})
