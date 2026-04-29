export type ReviewProgress = {
  reviewId: string
  status: 'pending' | 'completed'
  masteryScore: number
}

export const createReviewProgressRepository = (db: D1Database) => ({
  async getByUser(userId: string): Promise<Map<string, ReviewProgress>> {
    const rows = await db
      .prepare('SELECT review_id, status, mastery_score FROM review_progress WHERE user_id = ?')
      .bind(userId)
      .all<{ review_id: string; status: string; mastery_score: number }>()
    return new Map(
      (rows.results ?? []).map((row) => [
        row.review_id,
        {
          reviewId: row.review_id,
          status: row.status === 'completed' ? 'completed' : 'pending',
          masteryScore: Number(row.mastery_score ?? 0),
        },
      ]),
    )
  },
  async complete(userId: string, reviewId: string): Promise<ReviewProgress> {
    const now = new Date().toISOString()
    const existing = await db
      .prepare('SELECT mastery_score FROM review_progress WHERE user_id = ? AND review_id = ?')
      .bind(userId, reviewId)
      .first<{ mastery_score: number }>()
    const masteryScore = Math.min(Number(existing?.mastery_score ?? 0) + 1, 3)
    await db
      .prepare(
        'INSERT INTO review_progress (user_id, review_id, status, mastery_score, completed_at, updated_at) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT(user_id, review_id) DO UPDATE SET status = excluded.status, mastery_score = excluded.mastery_score, completed_at = excluded.completed_at, updated_at = excluded.updated_at',
      )
      .bind(userId, reviewId, 'completed', masteryScore, now, now)
      .run()
    return { reviewId, status: 'completed', masteryScore }
  },
})
