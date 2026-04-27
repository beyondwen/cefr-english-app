import type { LessonInstance, LessonPayload } from '../domain/types'

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
  async findActiveByUserId(userId: string): Promise<LessonInstance | null> {
    const row = await db
      .prepare(
        "SELECT lesson_json, status, template_id, generation_version, level FROM lesson_instances WHERE user_id = ? AND status != 'completed' ORDER BY generated_at DESC LIMIT 1",
      )
      .bind(userId)
      .first<{ lesson_json: string }>()
    return row ? (JSON.parse(row.lesson_json) as LessonInstance) : null
  },
  async insertActiveInstance(lesson: LessonInstance): Promise<void> {
    await db
      .prepare(
        'INSERT OR REPLACE INTO lesson_instances (lesson_instance_id, user_id, template_id, level, generation_version, status, lesson_json, generated_at, completed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
      )
      .bind(
        lesson.lessonInstanceId,
        lesson.userId,
        lesson.templateId,
        lesson.level,
        lesson.generationVersion,
        lesson.status,
        JSON.stringify(lesson),
        lesson.generatedAt,
        lesson.completedAt ?? null,
      )
      .run()
  },
})
