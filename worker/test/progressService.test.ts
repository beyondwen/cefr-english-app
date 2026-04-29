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
          lastCompletedDate: null,
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
          lastCompletedDate: null,
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

  it('does not complete the lesson when required answers are blank', async () => {
    const result = await submitLesson({
      userId: 'u1',
      lessonInstanceId: 'u1:A1-01:v1',
      readingAnswers: ['a', '', 'c'],
      grammarAnswers: ['a', 'b', ' '],
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
          lastCompletedDate: null,
        }),
        upsert: async () => undefined,
      },
      reviewResult: {
        ruleChecks: { notBlank: true, minSentencesOk: true, onTopicLikely: true },
      },
    })

    expect(result.completed).toBe(false)
    expect(result.missingRequirements).toEqual(['reading_incomplete', 'grammar_incomplete'])
  })

  it('does not complete the lesson when reading or grammar answers are incorrect', async () => {
    const result = await submitLesson({
      userId: 'u1',
      lessonInstanceId: 'u1:A1-01:v1',
      readingAnswers: ['wrong', 'blue', 'three'],
      grammarAnswers: ['am', 'wrong', 'is'],
      lessonRepo: {
        findByInstanceId: async () => ({
          templateId: 'A1-01',
          level: 'A1',
          writingPrompt: 'Write about your morning.',
          readingQuestions: [
            { questionId: 'r1', prompt: 'What is your name?', choices: [], answer: 'Anna' },
            { questionId: 'r2', prompt: 'What color is it?', choices: [], answer: 'blue' },
            { questionId: 'r3', prompt: 'How many books?', choices: [], answer: 'three' },
          ],
          grammarQuestions: [
            { questionId: 'g1', prompt: 'I ___ Anna.', choices: [], answer: 'am' },
            { questionId: 'g2', prompt: 'She ___ a teacher.', choices: [], answer: 'is' },
            { questionId: 'g3', prompt: 'It ___ red.', choices: [], answer: 'is' },
          ],
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
          lastCompletedDate: null,
        }),
        upsert: async () => undefined,
      },
      reviewResult: {
        ruleChecks: { notBlank: true, minSentencesOk: true, onTopicLikely: true },
      },
    })

    expect(result.completed).toBe(false)
    expect(result.missingRequirements).toEqual(['reading_incorrect', 'grammar_incorrect'])
    expect(result.answerFeedback).toEqual({
      reading: [
        {
          questionId: 'r1',
          prompt: 'What is your name?',
          expectedAnswer: 'Anna',
          submittedAnswer: 'wrong',
          correct: false,
        },
      ],
      grammar: [
        {
          questionId: 'g2',
          prompt: 'She ___ a teacher.',
          expectedAnswer: 'is',
          submittedAnswer: 'wrong',
          correct: false,
        },
      ],
    })
  })

  it('requires a writing revision before completing the lesson', async () => {
    const result = await submitLesson({
      userId: 'u1',
      lessonInstanceId: 'u1:A1-01:v1',
      readingAnswers: ['a', 'b', 'c'],
      grammarAnswers: ['a', 'b', 'c'],
      writingRevision: '',
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
          lastCompletedDate: null,
        }),
        upsert: async () => undefined,
      },
      reviewResult: {
        ruleChecks: { notBlank: true, minSentencesOk: true, onTopicLikely: true },
      },
    })

    expect(result.completed).toBe(false)
    expect(result.missingRequirements).toEqual(['writing_revision_required'])
  })

  it.each([
    ['omitted', undefined],
    ['null', null],
  ])('does not require a writing revision when it is %s', async (_label, writingRevision) => {
    const result = await submitLesson({
      userId: 'u1',
      lessonInstanceId: 'u1:A1-01:v1',
      readingAnswers: ['a', 'b', 'c'],
      grammarAnswers: ['a', 'b', 'c'],
      writingRevision,
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
          lastCompletedDate: null,
        }),
        upsert: async () => undefined,
      },
      reviewResult: {
        ruleChecks: { notBlank: true, minSentencesOk: true, onTopicLikely: true },
      },
    })

    expect(result.completed).toBe(true)
    expect(result.missingRequirements).toEqual([])
  })

  it('completes the lesson after the writing revision is provided', async () => {
    const result = await submitLesson({
      userId: 'u1',
      lessonInstanceId: 'u1:A1-01:v1',
      readingAnswers: ['a', 'b', 'c'],
      grammarAnswers: ['a', 'b', 'c'],
      writingRevision: 'I get up at seven. I eat breakfast at home.',
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
          lastCompletedDate: null,
        }),
        upsert: async () => undefined,
      },
      reviewResult: {
        ruleChecks: { notBlank: true, minSentencesOk: true, onTopicLikely: true },
      },
    })

    expect(result.completed).toBe(true)
    expect(result.revisionRequired).toBe(false)
    expect(result.missingRequirements).toEqual([])
  })
})
