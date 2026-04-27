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
    weaknesses?: string[]
    theme?: string
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
  async generateLesson({ lessonId, grammarFocus, writingTask, theme, weaknesses }) {
    const weaknessText = weaknesses?.length ? `Focus more on ${weaknesses.join(', ')}.` : 'Balanced practice.'
    return {
      readingText: `Sample reading for ${lessonId} about ${theme ?? 'daily life'}.`,
      readingQuestions: ['What is the main idea?', 'Which detail is correct?'],
      grammarExplanation: `Focus on ${grammarFocus}. ${weaknessText}`,
      grammarQuestions: ['Choose the correct form.', 'Rewrite the sentence correctly.'],
      writingPrompt: `Write a ${writingTask} response about ${theme ?? 'daily life'}.`,
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
