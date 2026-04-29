import type { LessonAnswerFeedback, ReviewItem, Weakness } from '../domain/types'
import type { WritingIssue } from '../providers/aiProvider'

type ReviewItemRow = {
  review_id: string
  skill: string
  title: string
  task: string
  steps_json: string
  due_date: string
}

const skillTitle: Record<'reading' | 'grammar', string> = {
  reading: '订正阅读错题',
  grammar: '订正语法错题',
}

const buildAnswerReviewItem = (
  userId: string,
  lessonInstanceId: string,
  skill: 'reading' | 'grammar',
  feedback: LessonAnswerFeedback,
): Omit<ReviewItem, 'status' | 'masteryScore'> => ({
  reviewId: `answer:${lessonInstanceId}:${skill}:${feedback.questionId}`,
  skill,
  title: skillTitle[skill],
  task: `重做：${feedback.prompt}`,
  steps: [
    `你上次写的是：${feedback.submittedAnswer || '空白'}`,
    `标准答案：${feedback.expectedAnswer}`,
    '遮住标准答案，重新写 1 遍，并说出为什么这样改。',
  ],
})

const mapRow = (row: ReviewItemRow): Omit<ReviewItem, 'status' | 'masteryScore'> => ({
  reviewId: row.review_id,
  skill: row.skill as Weakness,
  title: row.title,
  task: row.task,
  steps: JSON.parse(row.steps_json) as string[],
  dueDate: row.due_date,
})

const currentDateKey = (date = new Date()): string => date.toISOString().slice(0, 10)

const addDays = (dateKey: string, days: number): string => {
  const date = new Date(`${dateKey}T00:00:00.000Z`)
  date.setUTCDate(date.getUTCDate() + days)
  return currentDateKey(date)
}

export const createReviewItemRepository = (db: D1Database) => ({
  async insertFromAnswerFeedback(input: {
    userId: string
    lessonInstanceId: string
    answerFeedback: {
      reading: LessonAnswerFeedback[]
      grammar: LessonAnswerFeedback[]
    }
  }): Promise<void> {
    const now = new Date().toISOString()
    const dueDate = currentDateKey()
    const items = [
      ...input.answerFeedback.reading.map((feedback) => buildAnswerReviewItem(input.userId, input.lessonInstanceId, 'reading', feedback)),
      ...input.answerFeedback.grammar.map((feedback) => buildAnswerReviewItem(input.userId, input.lessonInstanceId, 'grammar', feedback)),
    ]
    for (const item of items) {
      await db
        .prepare(
          'INSERT INTO review_items (user_id, review_id, skill, title, task, steps_json, due_date, source_lesson_instance_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(user_id, review_id) DO UPDATE SET title = excluded.title, task = excluded.task, steps_json = excluded.steps_json, due_date = excluded.due_date, updated_at = excluded.updated_at',
        )
        .bind(input.userId, item.reviewId, item.skill, item.title, item.task, JSON.stringify(item.steps), dueDate, input.lessonInstanceId, now, now)
        .run()
    }
  },

  async insertFromWritingIssues(input: {
    userId: string
    lessonInstanceId: string
    issues: WritingIssue[]
  }): Promise<void> {
    const now = new Date().toISOString()
    const dueDate = currentDateKey()
    for (const [index, issue] of input.issues.entries()) {
      const reviewId = `writing:${input.lessonInstanceId}:${index}`
      await db
        .prepare(
          'INSERT INTO review_items (user_id, review_id, skill, title, task, steps_json, due_date, source_lesson_instance_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(user_id, review_id) DO UPDATE SET title = excluded.title, task = excluded.task, steps_json = excluded.steps_json, due_date = excluded.due_date, updated_at = excluded.updated_at',
        )
        .bind(
          input.userId,
          reviewId,
          'writing',
          `订正写作问题：${issue.errorType}`,
          issue.practicePrompt,
          JSON.stringify([
            `原句：${issue.originalText || '未定位到原句'}`,
            `建议：${issue.correction || '按反馈重写这句话'}`,
            issue.explanation,
          ].filter(Boolean)),
          dueDate,
          input.lessonInstanceId,
          now,
          now,
        )
        .run()
    }
  },

  async getByUser(userId: string): Promise<Array<Omit<ReviewItem, 'status' | 'masteryScore'>>> {
    const rows = await db
      .prepare('SELECT review_id, skill, title, task, steps_json, due_date FROM review_items WHERE user_id = ? AND due_date <= ? ORDER BY due_date ASC, updated_at DESC')
      .bind(userId, currentDateKey())
      .all<ReviewItemRow>()
    return (rows.results ?? []).map(mapRow)
  },

  async scheduleNext(userId: string, reviewId: string): Promise<void> {
    const now = new Date().toISOString()
    const nextDueDate = addDays(currentDateKey(), 3)
    await db
      .prepare('UPDATE review_items SET due_date = ?, updated_at = ? WHERE user_id = ? AND review_id = ?')
      .bind(nextDueDate, now, userId, reviewId)
      .run()
  },
})
