export type CefrLevel = 'A1' | 'A2' | 'B1'

export type Weakness = 'reading' | 'grammar' | 'writing'

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
  weaknesses: Weakness[]
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

export type LessonTemplate = {
  templateId: string
  level: CefrLevel
  sequence: number
  grammarFocus: string
  readingTaskType: 'reading_mcq'
  writingTaskType: 'short_paragraph'
  themePool: string[]
  targetWordRange: {
    min: number
    max: number
  }
  questionCounts: {
    reading: number
    grammar: number
  }
}

export type LessonQuestion = {
  questionId: string
  prompt: string
  choices: string[]
  answer: string
}

export type LessonInstance = {
  lessonInstanceId: string
  userId: string
  templateId: string
  level: CefrLevel
  theme: string
  readingText: string
  readingQuestions: LessonQuestion[]
  grammarExplanation: string
  grammarQuestions: LessonQuestion[]
  writingPrompt: string
  writingRubric: string[]
  status: 'generated' | 'in_progress' | 'completed'
  generationVersion: number
  generatedAt: string
  completedAt?: string | null
}

export type UserProfile = {
  userId: string
  currentLevel: CefrLevel
  recentWeaknesses: Weakness[]
  themeRotationState: 'life' | 'expression'
  currentTemplateId: string | null
}
