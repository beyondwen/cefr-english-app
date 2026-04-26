import type { WritingReview } from '../providers/aiProvider'

export const reviewWriting = async (input: {
  level: string
  prompt: string
  submission: string
  aiProvider: { generateWritingReview(input: { level: string; prompt: string; submission: string }): Promise<WritingReview> }
}) => {
  const trimmed = input.submission.trim()
  const wordCount = trimmed.length === 0 ? 0 : trimmed.split(/\s+/).length
  const ruleChecks = {
    wordCountOk: wordCount >= 8,
    notBlank: trimmed.length > 0,
    onTopicLikely: trimmed.length >= 15,
  }
  const feedback = await input.aiProvider.generateWritingReview({
    level: input.level,
    prompt: input.prompt,
    submission: input.submission,
  })
  return { ruleChecks, feedback }
}
