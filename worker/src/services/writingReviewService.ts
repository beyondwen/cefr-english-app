import type { WritingRuleChecks } from '../domain/types'
import type { WritingReview } from '../providers/aiProvider'

export const reviewWriting = async (input: {
  level: string
  prompt: string
  submission: string
  aiProvider: { generateWritingReview(input: { level: string; prompt: string; submission: string }): Promise<WritingReview> }
}) => {
  const trimmed = input.submission.trim()
  const sentenceCount = trimmed.length === 0 ? 0 : trimmed.split(/[.!?]+/).map((item) => item.trim()).filter(Boolean).length
  const ruleChecks: WritingRuleChecks = {
    notBlank: trimmed.length > 0,
    minSentencesOk: sentenceCount >= 2,
    onTopicLikely: trimmed.length >= 15,
  }
  const feedback = await input.aiProvider.generateWritingReview({
    level: input.level,
    prompt: input.prompt,
    submission: input.submission,
  })
  return { ruleChecks, feedback }
}
