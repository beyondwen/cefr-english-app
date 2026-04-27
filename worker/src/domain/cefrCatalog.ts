import type { CefrLevel, LessonBlueprint } from './types'

export const lessonBlueprints: Record<CefrLevel, LessonBlueprint[]> = {
  A1: [
    { lessonId: 'A1-01', unitIndex: 1, grammarFocus: 'be', writingTask: 'self_intro' },
    { lessonId: 'A1-02', unitIndex: 2, grammarFocus: 'present_simple', writingTask: 'daily_routine' },
  ],
  A2: [
    { lessonId: 'A2-01', unitIndex: 1, grammarFocus: 'past_simple', writingTask: 'past_experience' },
    { lessonId: 'A2-02', unitIndex: 2, grammarFocus: 'comparatives', writingTask: 'short_email' },
  ],
  B1: [
    { lessonId: 'B1-01', unitIndex: 1, grammarFocus: 'present_perfect', writingTask: 'personal_story' },
    { lessonId: 'B1-02', unitIndex: 2, grammarFocus: 'first_conditional', writingTask: 'solution_paragraph' },
  ],
  B2: [
    { lessonId: 'B2-01', unitIndex: 1, grammarFocus: 'complex_clauses', writingTask: 'balanced_argument' },
    { lessonId: 'B2-02', unitIndex: 2, grammarFocus: 'passive_voice', writingTask: 'proposal_summary' },
  ],
  C1: [
    { lessonId: 'C1-01', unitIndex: 1, grammarFocus: 'hedging', writingTask: 'analytical_paragraph' },
    { lessonId: 'C1-02', unitIndex: 2, grammarFocus: 'inversion', writingTask: 'complex_response' },
  ],
  C2: [
    { lessonId: 'C2-01', unitIndex: 1, grammarFocus: 'rhetorical_devices', writingTask: 'persuasive_opening' },
    { lessonId: 'C2-02', unitIndex: 2, grammarFocus: 'style_shift', writingTask: 'editorial_revision' },
  ],
}
