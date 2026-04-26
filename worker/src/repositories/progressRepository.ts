export const createProgressRepository = (db: D1Database) => ({
  async get(userId: string): Promise<{ completedLessonIds: string[]; level: string } | null> {
    const row = await db
      .prepare('SELECT completed_lesson_ids_json, level FROM progress WHERE user_id = ?')
      .bind(userId)
      .first<{ completed_lesson_ids_json: string; level: string }>()
    return row ? { completedLessonIds: JSON.parse(row.completed_lesson_ids_json) as string[], level: row.level } : null
  },
  async upsert(userId: string, level: string, completedLessonIds: string[]): Promise<void> {
    await db
      .prepare('INSERT INTO progress (user_id, level, completed_lesson_ids_json, updated_at) VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET level = excluded.level, completed_lesson_ids_json = excluded.completed_lesson_ids_json, updated_at = excluded.updated_at')
      .bind(userId, level, JSON.stringify(completedLessonIds), new Date().toISOString())
      .run()
  },
})
