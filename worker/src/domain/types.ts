export type CefrLevel = 'A1' | 'A2'

export type PlacementAnswer = {
  skill: 'reading' | 'grammar'
  correct: number
  total: number
}

export type PlacementRequest = {
  userId: string
  answers: PlacementAnswer[]
  writingWordCount: number
}

export type PlacementResult = {
  level: CefrLevel
  weaknesses: Array<'reading' | 'grammar' | 'writing'>
}

export type LessonBlueprint = {
  lessonId: string
  unitIndex: number
  grammarFocus: string
  writingTask: string
}

export type LessonPayload = {
  lessonId: string
  level: CefrLevel
  unitIndex: number
  grammarFocus: string
  readingText: string
  readingQuestions: string[]
  grammarExplanation: string
  grammarQuestions: string[]
  writingPrompt: string
}
