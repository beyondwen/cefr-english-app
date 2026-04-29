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

  it('can place strong learners above A2', () => {
    const result = assessPlacement({
      userId: 'u2',
      answers: [
        { skill: 'reading', correct: 9, total: 10 },
        { skill: 'grammar', correct: 9, total: 10 },
      ],
      writingWordCount: 180,
    })

    expect(result.level).toBe('C1')
    expect(result.weaknesses).toEqual([])
  })

  it('keeps short writing as a writing weakness even with decent answers', () => {
    const result = assessPlacement({
      userId: 'u3',
      answers: [
        { skill: 'reading', correct: 8, total: 10 },
        { skill: 'grammar', correct: 8, total: 10 },
      ],
      writingWordCount: 35,
    })

    expect(result.level).toBe('B1')
    expect(result.weaknesses).toEqual(['writing'])
  })
})
