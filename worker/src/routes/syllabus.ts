import type { Env } from '../env'
import type { CefrLevel } from '../domain/types'
import { json } from '../lib/json'
import { createAiProvider } from '../providers/aiProvider'
import { SyllabusRepository } from '../repositories/syllabusRepository'

const cefrLevels = new Set(['A1', 'A2', 'B1', 'B2', 'C1', 'C2'])

export const handleSyllabus = async (request: Request, env: Env): Promise<Response> => {
  const url = new URL(request.url)
  const level = url.searchParams.get('level')
  if (!level || !cefrLevels.has(level)) {
    return json({ error: 'Invalid CEFR level' }, 400)
  }

  const repository = new SyllabusRepository(env.DB)
  const existing = await repository.findByLevel(level as CefrLevel)
  if (existing) return json(existing)

  const generated = await createAiProvider(env).generateSyllabus({ level: level as CefrLevel })
  await repository.upsert(generated)
  return json(generated)
}

export const handleRegenerateSyllabus = async (request: Request, env: Env): Promise<Response> => {
  const payload = (await request.json()) as { level?: string }
  if (!payload.level || !cefrLevels.has(payload.level)) {
    return json({ error: 'Invalid CEFR level' }, 400)
  }

  const generated = await createAiProvider(env).generateSyllabus({ level: payload.level as CefrLevel })
  await new SyllabusRepository(env.DB).upsert(generated)
  return json(generated)
}
