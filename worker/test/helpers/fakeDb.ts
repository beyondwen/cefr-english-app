type Row = Record<string, unknown>

type TableState = {
  users: Map<string, { current_level: string; recent_weaknesses_json: string; theme_rotation_state: string; current_template_id: string | null }>
  placement_results: Row[]
  lessons: Map<string, { lesson_json: string }>
  lesson_instances: Map<string, { lesson_json: string; status: string; template_id: string; generation_version: number; level: string }>
  lesson_submissions: Row[]
  writing_reviews: Row[]
  review_progress: Map<string, { user_id: string; review_id: string; status: string; mastery_score: number }>
  progress: Map<
    string,
    {
      level: string
      completed_lesson_ids_json: string
      current_template_id: string | null
      current_lesson_instance_id: string | null
      today_completed: number
      last_completed_date: string | null
    }
  >
  course_syllabuses: Map<string, { level: string; title: string; description: string; modules_json: string }>
}

export const createFakeEnv = () => {
  const state: TableState = {
    users: new Map(),
    placement_results: [],
    lessons: new Map(),
    lesson_instances: new Map(),
    lesson_submissions: [],
    writing_reviews: [],
    review_progress: new Map(),
    progress: new Map(),
    course_syllabuses: new Map([
      [
        'A1',
        {
          level: 'A1',
          title: 'A1 入门基础',
          description: '建立简单日常场景中的基础英语表达能力。',
          modules_json: JSON.stringify([
            { title: '自我介绍', goal: '说明自己是谁，并提出简单问题。', lessons: ['be 动词与主语代词'] },
          ]),
        },
      ],
    ]),
  }

  const DB = {
    prepare(sql: string) {
      return {
        bind(...params: unknown[]) {
          return {
            async run() {
              if (sql.includes('INSERT INTO users')) {
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
              if (sql.startsWith('UPDATE lesson_instances SET status =')) {
                const existing = state.lesson_instances.get(String(params[2]))
                if (existing) {
                  existing.status = String(params[0])
                  const lesson = JSON.parse(existing.lesson_json) as { status: string; completedAt: string | null }
                  lesson.status = String(params[0])
                  lesson.completedAt = params[1] == null ? null : String(params[1])
                  existing.lesson_json = JSON.stringify(lesson)
                }
              }
              if (sql.startsWith('INSERT INTO writing_reviews')) {
                state.writing_reviews.push({ user_id: params[0], lesson_id: params[1] })
              }
              if (sql.startsWith('INSERT INTO review_progress')) {
                state.review_progress.set(`${params[0]}:${params[1]}`, {
                  user_id: String(params[0]),
                  review_id: String(params[1]),
                  status: String(params[2]),
                  mastery_score: Number(params[3]),
                })
              }
              if (sql.startsWith('INSERT INTO progress') || sql.startsWith('INSERT OR REPLACE INTO progress')) {
                state.progress.set(String(params[0]), {
                  level: String(params[1]),
                  completed_lesson_ids_json: String(params[2]),
                  current_template_id: params[3] == null ? null : String(params[3]),
                  current_lesson_instance_id: params[4] == null ? null : String(params[4]),
                  today_completed: typeof params[5] === 'number' ? Number(params[5]) : 0,
                  last_completed_date: params[6] == null ? null : String(params[6]),
                })
              }
              if (sql.startsWith('INSERT OR REPLACE INTO course_syllabuses')) {
                state.course_syllabuses.set(String(params[0]), {
                  level: String(params[0]),
                  title: String(params[1]),
                  description: String(params[2]),
                  modules_json: String(params[3]),
                })
              }
              return { success: true }
            },
            async first<T>() {
              if (sql.startsWith('SELECT current_level')) {
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
              if (sql.startsWith('SELECT lesson_json, status, template_id, generation_version, level FROM lesson_instances WHERE lesson_instance_id = ?')) {
                const row = state.lesson_instances.get(String(params[0]))
                return row
                  ? ({
                      lesson_json: row.lesson_json,
                      status: row.status,
                      template_id: row.template_id,
                      generation_version: row.generation_version,
                      level: row.level,
                    } as T)
                  : null
              }
              if (sql.startsWith("SELECT lesson_json, status, template_id, generation_version, level FROM lesson_instances WHERE user_id = ? AND status != 'completed'")) {
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
              if (sql.startsWith('SELECT completed_lesson_ids_json, level')) {
                const row = state.progress.get(String(params[0]))
                return row
                  ? ({
                      completed_lesson_ids_json: row.completed_lesson_ids_json,
                      level: row.level,
                      current_template_id: row.current_template_id,
                      current_lesson_instance_id: row.current_lesson_instance_id,
                      today_completed: row.today_completed,
                      last_completed_date: row.last_completed_date,
                    } as T)
                  : null
              }
              if (sql.startsWith('SELECT mastery_score FROM review_progress')) {
                const row = state.review_progress.get(`${params[0]}:${params[1]}`)
                return row ? ({ mastery_score: row.mastery_score } as T) : null
              }
              if (sql.startsWith('SELECT level, title, description, modules_json FROM course_syllabuses')) {
                const row = state.course_syllabuses.get(String(params[0]))
                return row ? ({ ...row } as T) : null
              }
              return null
            },
            async all<T>() {
              if (sql.startsWith('SELECT review_id, status, mastery_score FROM review_progress')) {
                return {
                  results: [...state.review_progress.values()]
                    .filter((row) => row.user_id === String(params[0]))
                    .map((row) => ({
                      review_id: row.review_id,
                      status: row.status,
                      mastery_score: row.mastery_score,
                    })) as T[],
                }
              }
              return { results: [] as T[] }
            },
          }
        },
      }
    },
  }

  return { DB }
}
