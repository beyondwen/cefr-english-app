export const createUserRepository = (db: D1Database) => ({
  async upsert(userId: string, level: string): Promise<void> {
    const now = new Date().toISOString()
    await db
      .prepare(
        'INSERT INTO users (user_id, current_level, created_at, updated_at) VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET current_level = excluded.current_level, updated_at = excluded.updated_at',
      )
      .bind(userId, level, now, now)
      .run()
  },
  async get(userId: string): Promise<{ currentLevel: string } | null> {
    const row = await db.prepare('SELECT current_level FROM users WHERE user_id = ?').bind(userId).first<{ current_level: string }>()
    return row ? { currentLevel: row.current_level } : null
  },
})
