export const createProgressRepository = (db: D1Database) => ({
  async get(
    userId: string,
  ): Promise<{
    completedLessonIds: string[]
    level: string
    currentTemplateId: string | null
    currentLessonInstanceId: string | null
    todayCompleted: boolean
    lastCompletedDate: string | null
  } | null> {
    const row = await db
      .prepare('SELECT completed_lesson_ids_json, level, current_template_id, current_lesson_instance_id, today_completed, last_completed_date FROM progress WHERE user_id = ?')
      .bind(userId)
      .first<{
        completed_lesson_ids_json: string
        level: string
        current_template_id: string | null
        current_lesson_instance_id: string | null
        today_completed: number
        last_completed_date?: string | null
      }>()
    const today = new Date().toISOString().slice(0, 10)
    return row
      ? {
          completedLessonIds: JSON.parse(row.completed_lesson_ids_json) as string[],
          level: row.level,
          currentTemplateId: row.current_template_id,
          currentLessonInstanceId: row.current_lesson_instance_id,
          todayCompleted: row.last_completed_date ? row.last_completed_date === today : row.today_completed === 1,
          lastCompletedDate: row.last_completed_date ?? null,
        }
      : null
  },
  async upsert(input: {
    userId: string
    level: string
    completedLessonIds: string[]
    currentTemplateId: string | null
    currentLessonInstanceId: string | null
    lastCompletedDate: string | null
  }): Promise<void> {
    await db
      .prepare('INSERT INTO progress (user_id, level, completed_lesson_ids_json, current_template_id, current_lesson_instance_id, today_completed, last_completed_date, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET level = excluded.level, completed_lesson_ids_json = excluded.completed_lesson_ids_json, current_template_id = excluded.current_template_id, current_lesson_instance_id = excluded.current_lesson_instance_id, today_completed = excluded.today_completed, last_completed_date = excluded.last_completed_date, updated_at = excluded.updated_at')
      .bind(
        input.userId,
        input.level,
        JSON.stringify(input.completedLessonIds),
        input.currentTemplateId,
        input.currentLessonInstanceId,
        input.lastCompletedDate ? 1 : 0,
        input.lastCompletedDate,
        new Date().toISOString(),
      )
      .run()
  },
})
