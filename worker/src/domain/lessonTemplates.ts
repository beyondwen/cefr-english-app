import type { CefrLevel, LessonTemplate } from './types'

const createTemplate = (
  level: CefrLevel,
  sequence: number,
  grammarFocus: string,
  readingTaskType: LessonTemplate['readingTaskType'],
  writingTaskType: LessonTemplate['writingTaskType'],
  themePool: string[],
  targetWordRange: LessonTemplate['targetWordRange'],
  questionCounts: LessonTemplate['questionCounts'],
): LessonTemplate => ({
  templateId: `${level}-${String(sequence).padStart(2, '0')}`,
  level,
  sequence,
  grammarFocus,
  readingTaskType,
  writingTaskType,
  themePool,
  targetWordRange,
  questionCounts,
})

export const lessonTemplatesByLevel: Record<CefrLevel, LessonTemplate[]> = {
  A1: [
    createTemplate(
      'A1',
      1,
      'be',
      'reading_mcq',
      'short_paragraph',
      ['daily life', 'self introduction', 'family'],
      { min: 80, max: 120 },
      { reading: 3, grammar: 3 },
    ),
    createTemplate(
      'A1',
      2,
      'present_simple',
      'reading_mcq',
      'short_paragraph',
      ['routine', 'school', 'weekend'],
      { min: 90, max: 130 },
      { reading: 3, grammar: 3 },
    ),
  ],
  A2: [
    createTemplate(
      'A2',
      1,
      'past_simple',
      'reading_mcq',
      'short_paragraph',
      ['travel', 'past experience', 'holiday plan'],
      { min: 120, max: 160 },
      { reading: 4, grammar: 4 },
    ),
    createTemplate(
      'A2',
      2,
      'comparatives',
      'reading_mcq',
      'short_paragraph',
      ['shopping', 'city life', 'technology'],
      { min: 120, max: 160 },
      { reading: 4, grammar: 4 },
    ),
  ],
  B1: [
    createTemplate(
      'B1',
      1,
      'present_perfect',
      'reading_mcq',
      'short_paragraph',
      ['work life', 'learning goals', 'health habits'],
      { min: 160, max: 220 },
      { reading: 4, grammar: 4 },
    ),
    createTemplate(
      'B1',
      2,
      'first_conditional',
      'reading_mcq',
      'short_paragraph',
      ['decision making', 'future plan', 'daily challenge'],
      { min: 170, max: 230 },
      { reading: 4, grammar: 4 },
    ),
  ],
}

export const allLessonTemplates = Object.values(lessonTemplatesByLevel).flat()
