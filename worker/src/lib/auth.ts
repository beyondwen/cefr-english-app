import type { Env } from '../env'
import { json } from './json'

const publicPaths = new Set(['/health'])

export const authorize = (request: Request, env: Env): Response | null => {
  if (!env.API_TOKEN || publicPaths.has(new URL(request.url).pathname)) return null

  const expected = `Bearer ${env.API_TOKEN}`
  if (request.headers.get('Authorization') === expected) return null

  return json({ error: 'Unauthorized' }, 401)
}
