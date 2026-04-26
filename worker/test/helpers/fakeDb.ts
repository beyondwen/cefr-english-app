type Row = Record<string, unknown>

type TableState = {
  users: Map<string, { current_level: string }>
  placement_results: Row[]
  lessons: Map<string, { lesson_json: string }>
  writing_reviews: Row[]
  progress: Map<string, { level: string; completed_lesson_ids_json: string }>
}

export const createFakeEnv = () => {
  const state: TableState = {
    users: new Map(),
    placement_results: [],
    lessons: new Map(),
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
                state.users.set(String(params[0]), { current_level: String(params[1]) })
              }
              if (sql.startsWith('INSERT INTO placement_results')) {
                state.placement_results.push({ user_id: params[0], level: params[1] })
              }
              if (sql.startsWith('INSERT OR REPLACE INTO lessons')) {
                state.lessons.set(`${params[1]}:${params[0]}`, { lesson_json: String(params[5]) })
              }
              if (sql.startsWith('INSERT INTO writing_reviews')) {
                state.writing_reviews.push({ user_id: params[0], lesson_id: params[1] })
              }
              if (sql.startsWith('INSERT INTO progress') || sql.startsWith('INSERT OR REPLACE INTO progress')) {
                state.progress.set(String(params[0]), {
                  level: String(params[1]),
                  completed_lesson_ids_json: String(params[2]),
                })
              }
              return { success: true }
            },
            async first<T>() {
              if (sql.startsWith('SELECT current_level FROM users')) {
                const row = state.users.get(String(params[0]))
                return row ? ({ current_level: row.current_level } as T) : null
              }
              if (sql.startsWith('SELECT lesson_json FROM lessons')) {
                const row = state.lessons.get(`${params[0]}:${params[1]}`)
                return row ? ({ lesson_json: row.lesson_json } as T) : null
              }
              if (sql.startsWith('SELECT completed_lesson_ids_json, level FROM progress')) {
                const row = state.progress.get(String(params[0]))
                return row ? ({ completed_lesson_ids_json: row.completed_lesson_ids_json, level: row.level } as T) : null
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
