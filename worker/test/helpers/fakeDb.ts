type Row = Record<string, unknown>

type TableState = {
  users: Map<string, { current_level: string; recent_weaknesses_json: string; theme_rotation_state: string; current_template_id: string | null }>
  placement_results: Row[]
  lessons: Map<string, { lesson_json: string }>
  lesson_instances: Map<string, { lesson_json: string; status: string; template_id: string; generation_version: number; level: string }>
  lesson_submissions: Row[]
  writing_reviews: Row[]
  progress: Map<string, { level: string; completed_lesson_ids_json: string; current_template_id: string | null; current_lesson_instance_id: string | null; today_completed: number }>
}

export const createFakeEnv = () => {
  const state: TableState = {
    users: new Map(),
    placement_results: [],
    lessons: new Map(),
    lesson_instances: new Map(),
    lesson_submissions: [],
    writing_reviews: [],
    progress: new Map(),
  }

  const DB = {
    prepare(sql: string) {
      return {
        bind(...params: unknown[]) {
          return {
            async run() {
              if (sql.startsWith('INSERT INTO users')) {
                state.users.set(String(params[0]), {
                  current_level: String(params[1]),
                  recent_weaknesses_json: typeof params[2] === 'string' ? String(params[2]) : '[]',
                  theme_rotation_state: typeof params[3] === 'string' ? String(params[3]) : 'life',
                  current_template_id: params[4] == null ? null : String(params[4]),
                })
              }
              if (sql.startsWith('UPDATE users SET current_template_id')) {
                const existing = state.users.get(String(params[3]))
                if (existing) {
                  existing.current_template_id = params[0] == null ? null : String(params[0])
                  existing.theme_rotation_state = String(params[1])
                }
              }
              if (sql.startsWith('INSERT INTO placement_results')) {
                state.placement_results.push({ user_id: params[0], level: params[1] })
              }
              if (sql.startsWith('INSERT OR REPLACE INTO lessons')) {
                state.lessons.set(`${params[1]}:${params[0]}`, { lesson_json: String(params[5]) })
              }
              if (sql.startsWith('INSERT INTO lesson_instances') || sql.startsWith('INSERT OR REPLACE INTO lesson_instances')) {
                const lessonJson = String(params[6])
                const userKey = JSON.parse(lessonJson).userId as string
                for (const [instanceId, instance] of state.lesson_instances.entries()) {
                  if (instanceId.startsWith(`${userKey}:`) && instance.status !== 'completed') {
                    state.lesson_instances.delete(instanceId)
                  }
                }
                state.lesson_instances.set(String(params[0]), {
                  template_id: String(params[2]),
                  level: String(params[3]),
                  generation_version: Number(params[4]),
                  status: String(params[5]),
                  lesson_json: lessonJson,
                })
              }
              if (sql.startsWith('INSERT INTO lesson_submissions')) {
                state.lesson_submissions.push({
                  lesson_instance_id: params[0],
                  user_id: params[1],
                })
              }
              if (sql.startsWith('INSERT INTO writing_reviews')) {
                state.writing_reviews.push({ user_id: params[0], lesson_id: params[1] })
              }
              if (sql.startsWith('INSERT INTO progress') || sql.startsWith('INSERT OR REPLACE INTO progress')) {
                state.progress.set(String(params[0]), {
                  level: String(params[1]),
                  completed_lesson_ids_json: String(params[2]),
                  current_template_id: params[3] == null ? null : String(params[3]),
                  current_lesson_instance_id: params[4] == null ? null : String(params[4]),
                  today_completed: typeof params[5] === 'number' ? Number(params[5]) : 0,
                })
              }
              return { success: true }
            },
            async first<T>() {
              if (sql.startsWith('SELECT current_level FROM users')) {
                const row = state.users.get(String(params[0]))
                return row
                  ? ({
                      current_level: row.current_level,
                      recent_weaknesses_json: row.recent_weaknesses_json,
                      theme_rotation_state: row.theme_rotation_state,
                      current_template_id: row.current_template_id,
                    } as T)
                  : null
              }
              if (sql.startsWith('SELECT lesson_json FROM lessons')) {
                const row = state.lessons.get(`${params[0]}:${params[1]}`)
                return row ? ({ lesson_json: row.lesson_json } as T) : null
              }
              if (sql.startsWith('SELECT lesson_json, status, template_id, generation_version, level FROM lesson_instances')) {
                const userId = String(params[0])
                for (const row of state.lesson_instances.values()) {
                  const lesson = JSON.parse(row.lesson_json) as { userId: string }
                  if (lesson.userId === userId && row.status !== 'completed') {
                    return ({
                      lesson_json: row.lesson_json,
                      status: row.status,
                      template_id: row.template_id,
                      generation_version: row.generation_version,
                      level: row.level,
                    } as T)
                  }
                }
                return null
              }
              if (sql.startsWith('SELECT completed_lesson_ids_json, level FROM progress')) {
                const row = state.progress.get(String(params[0]))
                return row
                  ? ({
                      completed_lesson_ids_json: row.completed_lesson_ids_json,
                      level: row.level,
                      current_template_id: row.current_template_id,
                      current_lesson_instance_id: row.current_lesson_instance_id,
                      today_completed: row.today_completed,
                    } as T)
                  : null
              }
              return null
            },
          }
        },
      }
    },
  }

  return { DB }
}
