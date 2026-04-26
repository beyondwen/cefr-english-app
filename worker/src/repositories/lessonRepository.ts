import type { LessonPayload } from '../domain/types'

export const createLessonRepository = (db: D1Database) => ({
  async findByLessonId(userId: string, lessonId: string): Promise<LessonPayload | null> {
    const row = await db
      .prepare('SELECT lesson_json FROM lessons WHERE user_id = ? AND lesson_id = ?')
      .bind(userId, lessonId)
      .first<{ lesson_json: string }>()
    return row ? (JSON.parse(row.lesson_json) as LessonPayload) : null
  },
  async insert(userId: string, lesson: LessonPayload): Promise<void> {
    await db
      .prepare('INSERT OR REPLACE INTO lessons (lesson_id, user_id, level, unit_index, status, lesson_json, generated_by, generated_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)')
      .bind(
        lesson.lessonId,
        userId,
        lesson.level,
        lesson.unitIndex,
        'ready',
        JSON.stringify(lesson),
        'fake-provider',
        new Date().toISOString(),
        'v1',
      )
      .run()
  },
})
