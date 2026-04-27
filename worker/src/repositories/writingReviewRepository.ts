import type { WritingReview } from '../providers/aiProvider'

export const createWritingReviewRepository = (db: D1Database) => ({
  async insert(userId: string, lessonId: string, prompt: string, submission: string, review: WritingReview): Promise<void> {
    await db
      .prepare('INSERT INTO writing_reviews (user_id, lesson_id, prompt, submission, review_json, created_at) VALUES (?, ?, ?, ?, ?, ?)')
      .bind(userId, lessonId, prompt, submission, JSON.stringify(review), new Date().toISOString())
      .run()
  },
  async insertSubmission(
    lessonInstanceId: string,
    userId: string,
    readingAnswers: string[],
    grammarAnswers: string[],
    writingSubmission: string,
    review: WritingReview,
  ): Promise<void> {
    await db
      .prepare('INSERT INTO lesson_submissions (lesson_instance_id, user_id, reading_answers_json, grammar_answers_json, writing_submission, review_json, submitted_at) VALUES (?, ?, ?, ?, ?, ?, ?)')
      .bind(
        lessonInstanceId,
        userId,
        JSON.stringify(readingAnswers),
        JSON.stringify(grammarAnswers),
        writingSubmission,
        JSON.stringify(review),
        new Date().toISOString(),
      )
      .run()
  },
})
