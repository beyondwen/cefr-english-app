import { describe, expect, it } from 'vitest'
import { getNextLesson } from '../src/services/lessonService'

const fakeProvider = {
  async generateLesson() {
    return {
      readingText: 'Anna visits the market every Saturday.',
      readingQuestions: ['Where does Anna go?'],
      grammarExplanation: 'Use present simple for routines.',
      grammarQuestions: ['Choose the correct verb.'],
      writingPrompt: 'Write about your weekly routine.',
    }
  },
}

describe('getNextLesson', () => {
  it('returns first A1 lesson blueprint', async () => {
    const result = await getNextLesson({
      userId: 'u1',
      level: 'A1',
      lessonRepo: {
        findByLessonId: async () => null,
        insert: async () => undefined,
      },
      aiProvider: fakeProvider,
    })

    expect(result.lessonId).toBe('A1-01')
    expect(result.grammarFocus).toBe('be')
  })
})
