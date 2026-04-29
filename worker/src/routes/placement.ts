import type { PlacementRequest } from '../domain/types'
import type { Env } from '../env'
import { json } from '../lib/json'
import { createAiProvider } from '../providers/aiProvider'
import { createPlacementRepository } from '../repositories/placementRepository'
import { createUserRepository } from '../repositories/userRepository'
import { assessPlacement } from '../services/placementService'

export const handlePlacementTest = async (_request: Request, env: Env): Promise<Response> => {
  const test = await createAiProvider(env).generatePlacementTest()
  return json(test)
}

export const handlePlacement = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as PlacementRequest
  if (!payload.userId || !Array.isArray(payload.answers) || typeof payload.writingWordCount !== 'number') {
    return json({ error: 'Invalid placement request' }, 400)
  }
  const result = assessPlacement(payload)

  await createUserRepository(env.DB).upsert(payload.userId, result.level, result.weaknesses)
  await createPlacementRepository(env.DB).insert(payload.userId, result, payload.answers)

  return json(result)
}
