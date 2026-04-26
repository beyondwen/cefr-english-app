export type WritingReview = {
  grammar: string
  vocabulary: string
  coherence: string
  suggestions: string[]
  rewrite: string
}

export type AiProvider = {
  generateLesson(input: {
    level: string
    lessonId: string
    grammarFocus: string
    writingTask: string
  }): Promise<{
    readingText: string
    readingQuestions: string[]
    grammarExplanation: string
    grammarQuestions: string[]
    writingPrompt: string
  }>
  generateWritingReview(input: { level: string; prompt: string; submission: string }): Promise<WritingReview>
}

export const fakeAiProvider: AiProvider = {
  async generateLesson({ lessonId, grammarFocus, writingTask }) {
    return {
      readingText: `Sample reading for ${lessonId}`,
      readingQuestions: ['Question 1'],
      grammarExplanation: `Focus on ${grammarFocus}`,
      grammarQuestions: ['Question 2'],
      writingPrompt: `Write a ${writingTask} response.`,
    }
  },
  async generateWritingReview() {
    return {
      grammar: 'Good use of basic present simple.',
      vocabulary: 'Add more topic words.',
      coherence: 'The text is easy to follow.',
      suggestions: ['Link the two sentences with and.'],
      rewrite: 'I wake up early and I walk to school.',
    }
  },
}
