import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('API token auth', () => {
  it('allows public health checks without a token', async () => {
    const app = createApp({ ...createFakeEnv(), API_TOKEN: 'secret-token' })
    const response = await app.fetch(new Request('http://localhost/health'))

    expect(response.status).toBe(200)
  })

  it('rejects API requests without the bearer token', async () => {
    const app = createApp({ ...createFakeEnv(), API_TOKEN: 'secret-token' })
    const response = await app.fetch(new Request('http://localhost/api/syllabus?level=A1'))

    expect(response.status).toBe(401)
  })

  it('allows API requests with the bearer token', async () => {
    const app = createApp({ ...createFakeEnv(), API_TOKEN: 'secret-token' })
    const response = await app.fetch(
      new Request('http://localhost/api/syllabus?level=A1', {
        headers: { Authorization: 'Bearer secret-token' },
      }),
    )

    expect(response.status).toBe(200)
  })
})
