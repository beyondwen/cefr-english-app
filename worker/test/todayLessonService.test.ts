import { describe, expect, it } from 'vitest'
import { lessonTemplatesByLevel } from '../src/domain/lessonTemplates'

describe('lesson templates', () => {
  it('contains at least 2 templates for each A1/A2/B1 level', () => {
    expect(lessonTemplatesByLevel.A1.length).toBeGreaterThanOrEqual(2)
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
})
