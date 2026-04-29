import type { WritingRuleChecks } from '../domain/types'
import type { WritingReview } from '../providers/aiProvider'

const writingRequirementsByLevel: Record<string, { minSentences: number; minWords: number }> = {
  A1: { minSentences: 2, minWords: 20 },
  A2: { minSentences: 3, minWords: 60 },
  B1: { minSentences: 5, minWords: 100 },
  B2: { minSentences: 6, minWords: 150 },
  C1: { minSentences: 7, minWords: 180 },
  C2: { minSentences: 8, minWords: 220 },
}

const wordCount = (value: string): number => value.trim().split(/\s+/).filter(Boolean).length

export const reviewWriting = async (input: {
  level: string
  prompt: string
  submission: string
  aiProvider: { generateWritingReview(input: { level: string; prompt: string; submission: string }): Promise<WritingReview> }
}) => {
  const trimmed = input.submission.trim()
  const sentenceCount = trimmed.length === 0 ? 0 : trimmed.split(/[.!?]+/).map((item) => item.trim()).filter(Boolean).length
  const requirements = writingRequirementsByLevel[input.level] ?? writingRequirementsByLevel.A1
  const words = wordCount(trimmed)
  const ruleChecks: WritingRuleChecks = {
    notBlank: trimmed.length > 0,
    minSentencesOk: sentenceCount >= requirements.minSentences,
    onTopicLikely: trimmed.length >= 15,
    minWordsOk: words >= requirements.minWords,
    structureOk: input.level === 'A1' || input.level === 'A2' || sentenceCount >= requirements.minSentences,
  }
  const feedback = await input.aiProvider.generateWritingReview({
    level: input.level,
    prompt: input.prompt,
    submission: input.submission,
  })
  return { ruleChecks, feedback }
}
