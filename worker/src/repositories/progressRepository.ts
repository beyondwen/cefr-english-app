export const createProgressRepository = (db: D1Database) => ({
  async get(
    userId: string,
  ): Promise<{
    completedLessonIds: string[]
    level: string
    currentTemplateId: string | null
    currentLessonInstanceId: string | null
    todayCompleted: boolean
  } | null> {
    const row = await db
      .prepare('SELECT completed_lesson_ids_json, level, current_template_id, current_lesson_instance_id, today_completed FROM progress WHERE user_id = ?')
      .bind(userId)
      .first<{
        completed_lesson_ids_json: string
        level: string
        current_template_id: string | null
        current_lesson_instance_id: string | null
        today_completed: number
      }>()
    return row
      ? {
          completedLessonIds: JSON.parse(row.completed_lesson_ids_json) as string[],
          level: row.level,
          currentTemplateId: row.current_template_id,
          currentLessonInstanceId: row.current_lesson_instance_id,
          todayCompleted: row.today_completed === 1,
        }
      : null
  },
  async upsert(input: {
    userId: string
    level: string
    completedLessonIds: string[]
    currentTemplateId: string | null
    currentLessonInstanceId: string | null
    todayCompleted: boolean
  }): Promise<void> {
    await db
      .prepare('INSERT INTO progress (user_id, level, completed_lesson_ids_json, current_template_id, current_lesson_instance_id, today_completed, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET level = excluded.level, completed_lesson_ids_json = excluded.completed_lesson_ids_json, current_template_id = excluded.current_template_id, current_lesson_instance_id = excluded.current_lesson_instance_id, today_completed = excluded.today_completed, updated_at = excluded.updated_at')
      .bind(
        input.userId,
        input.level,
        JSON.stringify(input.completedLessonIds),
        input.currentTemplateId,
        input.currentLessonInstanceId,
        input.todayCompleted ? 1 : 0,
        new Date().toISOString(),
      )
      .run()
  },
})
