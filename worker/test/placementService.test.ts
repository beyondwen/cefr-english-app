import { describe, expect, it } from 'vitest'
import { assessPlacement } from '../src/services/placementService'

describe('assessPlacement', () => {
  it('returns A2 and grammar weakness for mixed answers', () => {
    const result = assessPlacement({
      userId: 'u1',
      answers: [
        { skill: 'reading', correct: 4, total: 5 },
        { skill: 'grammar', correct: 2, total: 5 },
      ],
      writingWordCount: 70,
    })

    expect(result.level).toBe('A2')
    expect(result.weaknesses).toEqual(['grammar'])
  })
})
