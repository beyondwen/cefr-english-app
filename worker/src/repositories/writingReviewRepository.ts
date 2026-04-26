import type { WritingReview } from '../providers/aiProvider'

export const createWritingReviewRepository = (db: D1Database) => ({
  async insert(userId: string, lessonId: string, prompt: string, submission: string, review: WritingReview): Promise<void> {
    await db
      .prepare('INSERT INTO writing_reviews (user_id, lesson_id, prompt, submission, review_json, created_at) VALUES (?, ?, ?, ?, ?, ?)')
      .bind(userId, lessonId, prompt, submission, JSON.stringify(review), new Date().toISOString())
      .run()
  },
})
