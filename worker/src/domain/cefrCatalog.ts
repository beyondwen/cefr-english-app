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
}
