import { describe, expect, it } from 'vitest'
import { completeLesson, submitLesson } from '../src/services/progressService'

describe('completeLesson', () => {
  it('returns next lesson id after completion', async () => {
    const result = await completeLesson({
      userId: 'u1',
      level: 'A1',
      lessonId: 'A1-01',
      progressRepo: {
        get: async () => ({
          completedLessonIds: [],
          level: 'A1',
          currentTemplateId: 'A1-01',
          currentLessonInstanceId: 'u1:A1-01:v1',
          todayCompleted: false,
        }),
        upsert: async () => undefined,
      },
    })

    expect(result.completedLessonIds).toEqual(['A1-01'])
    expect(result.nextLessonId).toBe('A1-02')
  })

  it('does not complete the lesson when writing has fewer than 2 sentences', async () => {
    const result = await submitLesson({
      userId: 'u1',
      lessonInstanceId: 'u1:A1-01:v1',
      readingAnswers: ['a', 'b', 'c'],
      grammarAnswers: ['a', 'b', 'c'],
      lessonRepo: {
        findByInstanceId: async () => ({
          templateId: 'A1-01',
          level: 'A1',
          writingPrompt: 'Write about your morning.',
        }),
        markCompleted: async () => undefined,
      },
      progressRepo: {
        get: async () => ({
          completedLessonIds: [],
          level: 'A1',
          currentTemplateId: 'A1-01',
          currentLessonInstanceId: 'u1:A1-01:v1',
          todayCompleted: false,
        }),
        upsert: async () => undefined,
      },
      reviewResult: {
        ruleChecks: { notBlank: true, minSentencesOk: false, onTopicLikely: true },
      },
    })

    expect(result.completed).toBe(false)
    expect(result.missingRequirements).toContain('writing_min_sentences')
  })
})
