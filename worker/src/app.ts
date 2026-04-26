import { handleHealth } from './routes/health'
import { handleNextLesson } from './routes/lessons'
import { handlePlacement } from './routes/placement'
import { handleCompleteLesson, handleSummary } from './routes/progress'
import { handleWritingReview } from './routes/writing'
import type { Env } from './env'

export const createApp = (env: Env) => ({
  async fetch(request: Request): Promise<Response> {
    const url = new URL(request.url)
    if (url.pathname === '/health') return handleHealth()
    if (url.pathname === '/api/placement/assess' && request.method === 'POST') return handlePlacement(request, env)
    if (url.pathname === '/api/lessons/next' && request.method === 'POST') return handleNextLesson(request, env)
    if (url.pathname === '/api/writing/review' && request.method === 'POST') return handleWritingReview(request, env)
    if (url.pathname === '/api/progress/complete' && request.method === 'POST') return handleCompleteLesson(request, env)
    if (url.pathname === '/api/me/summary' && request.method === 'GET') return handleSummary(request, env)
    return new Response('Not Found', { status: 404 })
  },
})
