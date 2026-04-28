import { describe, expect, it, vi } from 'vitest'
import { lessonTemplatesByLevel } from '../src/domain/lessonTemplates'
import { getTodayLesson, regenerateTodayLesson } from '../src/services/todayLessonService'

describe('lesson templates', () => {
  it('contains a full A1 self-study path and at least 2 templates for A2/B1', () => {
    expect(lessonTemplatesByLevel.A1.length).toBeGreaterThanOrEqual(10)
    expect(lessonTemplatesByLevel.A2.length).toBeGreaterThanOrEqual(2)
    expect(lessonTemplatesByLevel.B1.length).toBeGreaterThanOrEqual(2)
  })

  it('builds stable template ids and reading/grammar question counts', () => {
    const a1First = lessonTemplatesByLevel.A1[0]
    const b1Second = lessonTemplatesByLevel.B1[1]

    expect(a1First.templateId).toBe('A1-01')
    expect(a1First.questionCounts).toEqual({ reading: 3, grammar: 3 })
    expect(b1Second.templateId).toBe('B1-02')
    expect(b1Second.targetWordRange.min).toBeGreaterThanOrEqual(150)
  })

  it('defines teacher guidance for every A1 lesson', () => {
    for (const template of lessonTemplatesByLevel.A1) {
      expect(template.teachingGoal).toBeTruthy()
      expect(template.targetSentences?.length).toBeGreaterThanOrEqual(3)
      expect(template.reviewFocus?.length).toBeGreaterThanOrEqual(3)
    }
  })
})

describe('today lesson service', () => {
  it('returns the cached lesson instance when an unfinished lesson already exists', async () => {
    const aiProvider = {
      generateLesson: vi.fn(),
      generateWritingReview: vi.fn(),
    }
    const existingLesson = {
      lessonInstanceId: 'instance-1',
      userId: 'u1',
      templateId: 'A1-01',
      level: 'A1',
      theme: 'daily life',
      readingText: 'cached reading',
      readingQuestions: [],
      grammarExplanation: 'cached grammar',
      grammarQuestions: [],
      writingPrompt: 'cached prompt',
      writingRubric: [],
      status: 'generated',
      generationVersion: 1,
      generatedAt: '2026-04-27T00:00:00Z',
      completedAt: null,
    } as const

    const result = await getTodayLesson({
      userId: 'u1',
      profileRepo: {
        async getOrCreate() {
          throw new Error('should not load profile when cached lesson exists')
        },
        async updateCurrentTemplate() {},
      },
      lessonRepo: {
        async findActiveByUserId() {
          return existingLesson
        },
        async insertActiveInstance() {},
      },
      aiProvider,
    })

    expect(result.lessonInstanceId).toBe('instance-1')
    expect(aiProvider.generateLesson).toHaveBeenCalledTimes(0)
  })

  it('regenerates a new version for an unfinished lesson', async () => {
    const aiProvider = {
      async generateLesson() {
        return {
          readingText: 'new reading',
          readingQuestions: ['Q1', 'Q2'],
          grammarExplanation: 'new grammar',
          grammarQuestions: ['G1', 'G2'],
          writingPrompt: 'new prompt',
        }
      },
      generateWritingReview: vi.fn(),
    }
    const savedVersions: number[] = []

    const regenerated = await regenerateTodayLesson({
      userId: 'u1',
      profileRepo: {
        async getOrCreate() {
          return {
            userId: 'u1',
            currentLevel: 'A1',
            recentWeaknesses: ['grammar'],
            themeRotationState: 'life',
            currentTemplateId: 'A1-01',
          }
        },
        async updateCurrentTemplate() {},
      },
      lessonRepo: {
        async findActiveByUserId() {
          return {
            lessonInstanceId: 'u1:A1-01:v1',
            userId: 'u1',
            templateId: 'A1-01',
            level: 'A1',
            theme: 'daily life',
            readingText: 'cached',
            readingQuestions: [],
            grammarExplanation: 'cached',
            grammarQuestions: [],
            writingPrompt: 'cached',
            writingRubric: [],
            status: 'generated',
            generationVersion: 1,
            generatedAt: '2026-04-27T00:00:00Z',
            completedAt: null,
          }
        },
        async insertActiveInstance(lesson) {
          savedVersions.push(lesson.generationVersion)
        },
      },
      aiProvider,
    })

    expect(regenerated.generationVersion).toBe(2)
    expect(savedVersions).toEqual([2])
  })
})
