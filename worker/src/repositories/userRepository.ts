import type { UserProfile, Weakness } from '../domain/types'

const mapUserProfile = (row: {
  current_level: string
  recent_weaknesses_json?: string
  theme_rotation_state?: string
  current_template_id?: string | null
}): UserProfile => ({
  userId: '',
  currentLevel: row.current_level as UserProfile['currentLevel'],
  recentWeaknesses: JSON.parse(row.recent_weaknesses_json ?? '[]') as Weakness[],
  themeRotationState: (row.theme_rotation_state as UserProfile['themeRotationState'] | undefined) ?? 'life',
  currentTemplateId: row.current_template_id ?? null,
})

export const createUserRepository = (db: D1Database) => ({
  async upsert(userId: string, level: string, weaknesses: Weakness[] = [], currentTemplateId: string | null = null): Promise<void> {
    const now = new Date().toISOString()
    await db
      .prepare(
        'INSERT INTO users (user_id, current_level, recent_weaknesses_json, theme_rotation_state, current_template_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET current_level = excluded.current_level, recent_weaknesses_json = excluded.recent_weaknesses_json, current_template_id = excluded.current_template_id, updated_at = excluded.updated_at',
      )
      .bind(userId, level, JSON.stringify(weaknesses), 'life', currentTemplateId, now, now)
      .run()
  },
  async get(userId: string): Promise<(UserProfile & { currentLevel: string }) | null> {
    const row = await db
      .prepare('SELECT current_level, recent_weaknesses_json, theme_rotation_state, current_template_id FROM users WHERE user_id = ?')
      .bind(userId)
      .first<{
        current_level: string
        recent_weaknesses_json?: string
        theme_rotation_state?: string
        current_template_id?: string | null
      }>()
    if (!row) return null
    const profile = mapUserProfile(row)
    return {
      ...profile,
      userId,
      currentLevel: profile.currentLevel,
    }
  },
  async getOrCreate(userId: string): Promise<UserProfile> {
    const existing = await this.get(userId)
    if (existing) return existing

    await this.upsert(userId, 'A1')
    return {
      userId,
      currentLevel: 'A1',
      recentWeaknesses: [],
      themeRotationState: 'life',
      currentTemplateId: null,
    }
  },
  async updateCurrentTemplate(userId: string, templateId: string, themeRotationState: UserProfile['themeRotationState']): Promise<void> {
    const now = new Date().toISOString()
    await db
      .prepare(
        'UPDATE users SET current_template_id = ?, theme_rotation_state = ?, updated_at = ? WHERE user_id = ?',
      )
      .bind(templateId, themeRotationState, now, userId)
      .run()
  },
})
