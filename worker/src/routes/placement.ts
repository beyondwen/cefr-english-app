import type { PlacementRequest } from '../domain/types'
import type { Env } from '../env'
import { json } from '../lib/json'
import { createPlacementRepository } from '../repositories/placementRepository'
import { createUserRepository } from '../repositories/userRepository'
import { assessPlacement } from '../services/placementService'

export const handlePlacement = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as PlacementRequest
  const result = assessPlacement(payload)

  await createUserRepository(env.DB).upsert(payload.userId, result.level)
  await createPlacementRepository(env.DB).insert(payload.userId, result, payload.answers)

  return json(result)
}
