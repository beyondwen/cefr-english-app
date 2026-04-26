import { describe, expect, it } from 'vitest'
import { createApp } from '../src/app'
import { createFakeEnv } from './helpers/fakeDb'

describe('GET /health', () => {
  it('returns ok payload', async () => {
    const app = createApp(createFakeEnv())
    const response = await app.fetch(new Request('http://localhost/health'))

    expect(response.status).toBe(200)
    await expect(response.json()).resolves.toEqual({ status: 'ok' })
  })
})
