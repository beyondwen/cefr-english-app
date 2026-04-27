import type { Env } from '../env'
import type { CefrLevel } from '../domain/types'
import { json } from '../lib/json'
import { SyllabusRepository } from '../repositories/syllabusRepository'

const cefrLevels = new Set(['A1', 'A2', 'B1', 'B2', 'C1', 'C2'])

export const handleSyllabus = async (request: Request, env: Env): Promise<Response> => {
  const url = new URL(request.url)
  const level = url.searchParams.get('level')
  if (!level || !cefrLevels.has(level)) {
    return json({ error: 'Invalid CEFR level' }, 400)
  }

  const syllabus = await new SyllabusRepository(env.DB).findByLevel(level as CefrLevel)
  if (!syllabus) {
    return json({ error: 'Syllabus not found' }, 404)
  }

  return json(syllabus)
}
