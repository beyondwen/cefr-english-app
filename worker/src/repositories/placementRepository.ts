import type { PlacementResult } from '../domain/types'

export const createPlacementRepository = (db: D1Database) => ({
  async insert(userId: string, result: PlacementResult, rawScore: unknown): Promise<void> {
    await db
      .prepare('INSERT INTO placement_results (user_id, level, weaknesses_json, raw_score_json, created_at) VALUES (?, ?, ?, ?, ?)')
      .bind(userId, result.level, JSON.stringify(result.weaknesses), JSON.stringify(rawScore), new Date().toISOString())
      .run()
  },
})
