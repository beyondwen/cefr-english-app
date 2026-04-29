import { describe, expect, it } from 'vitest'
import { reviewWriting } from '../src/services/writingReviewService'

const fakeProvider = {
  async generateWritingReview() {
    return {
      grammar: 'Use present simple consistently.',
      vocabulary: 'Add more daily routine words.',
      coherence: 'The ideas are clear but short.',
      suggestions: ['Add one more sentence about the evening.'],
      rewrite: 'I wake up at seven and go to work by bus.',
    }
  },
}

describe('reviewWriting', () => {
  it('returns rule flags and ai feedback', async () => {
    const result = await reviewWriting({
      level: 'A1',
      prompt: 'Write about your daily routine.',
      submission: 'I wake up at 7 and go to school every day.',
      aiProvider: fakeProvider,
    })

    expect(result.ruleChecks.minSentencesOk).toBe(false)
    expect(result.feedback.grammar).toContain('present simple')
  })

  it('uses stricter word and sentence requirements for B1 writing', async () => {
    const result = await reviewWriting({
      level: 'B1',
      prompt: 'Write your opinion about online learning.',
      submission: 'Online learning is useful. It saves time. It is flexible.',
      aiProvider: fakeProvider,
    })

    expect(result.ruleChecks.minSentencesOk).toBe(false)
    expect(result.ruleChecks.minWordsOk).toBe(false)
    expect(result.ruleChecks.structureOk).toBe(false)
  })
})
