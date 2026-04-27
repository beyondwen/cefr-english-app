import type { CourseSyllabus, CefrLevel, SyllabusModule } from '../domain/types'

type SyllabusRow = {
  level: CefrLevel
  title: string
  description: string
  modules_json: string
}

export class SyllabusRepository {
  constructor(private readonly db: D1Database) {}

  async findByLevel(level: CefrLevel): Promise<CourseSyllabus | null> {
    const row = await this.db
      .prepare('SELECT level, title, description, modules_json FROM course_syllabuses WHERE level = ?')
      .bind(level)
      .first<SyllabusRow>()

    if (!row) return null
    return {
      level: row.level,
      title: row.title,
      description: row.description,
      modules: JSON.parse(row.modules_json) as SyllabusModule[],
    }
  }
}
