import type { PlacementRequest, PlacementResult } from '../domain/types'

const levelByScore = (score: number): PlacementResult['level'] => {
  if (score >= 0.92) return 'C2'
  if (score >= 0.82) return 'C1'
  if (score >= 0.72) return 'B2'
  if (score >= 0.6) return 'B1'
  if (score >= 0.42) return 'A2'
  return 'A1'
}

const writingScore = (wordCount: number): number => {
  if (wordCount >= 220) return 1
  if (wordCount >= 170) return 0.85
  if (wordCount >= 120) return 0.7
  if (wordCount >= 70) return 0.55
  if (wordCount >= 40) return 0.4
  return 0.2
}

export const assessPlacement = (request: PlacementRequest): PlacementResult => {
  const totalCorrect = request.answers.reduce((sum, item) => sum + item.correct, 0)
  const totalQuestions = request.answers.reduce((sum, item) => sum + item.total, 0)
  const accuracy = totalQuestions === 0 ? 0 : totalCorrect / totalQuestions
  const writing = writingScore(request.writingWordCount)
  const score = accuracy * 0.75 + writing * 0.25

  const weaknesses: Array<'reading' | 'grammar' | 'writing'> = []
  for (const answer of request.answers) {
    const ratio = answer.total > 0 ? answer.correct / answer.total : 0
    if (ratio < 0.6) weaknesses.push(answer.skill)
  }
  if (writing < 0.55) weaknesses.push('writing')

  return {
    level: levelByScore(score),
    weaknesses,
  }
}
