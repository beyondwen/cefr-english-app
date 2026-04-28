import type { PlacementRequest, PlacementResult } from '../domain/types'

export const assessPlacement = (request: PlacementRequest): PlacementResult => {
  const totalCorrect = request.answers.reduce((sum, item) => sum + item.correct, 0)
  const totalQuestions = request.answers.reduce((sum, item) => sum + item.total, 0)
  const ratio = totalQuestions === 0 ? 0 : totalCorrect / totalQuestions

  const weaknesses: Array<'reading' | 'grammar' | 'writing'> = []
  for (const answer of request.answers) {
    const ratio = answer.total > 0 ? answer.correct / answer.total : 0
    if (ratio < 0.6) weaknesses.push(answer.skill)
  }
  if (request.writingWordCount < 50) weaknesses.push('writing')

  return {
    level: ratio >= 0.6 && request.writingWordCount >= 50 ? 'A2' : 'A1',
    weaknesses,
  }
}
