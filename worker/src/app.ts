import { handleHealth } from './routes/health'
import { handleLessonSubmit } from './routes/lessonSubmit'
import { handleNextLesson, handleRegenerateTodayLesson, handleSyllabusModuleLesson, handleTodayLesson } from './routes/lessons'
import { handlePlacement, handlePlacementTest } from './routes/placement'
import { handleCompleteLesson, handleCompleteReview, handleSummary } from './routes/progress'
import { handleRegenerateSyllabus, handleSyllabus } from './routes/syllabus'
import { handleWritingReview } from './routes/writing'
import type { Env } from './env'
import { authorize } from './lib/auth'

export const createApp = (env: Env) => ({
  async fetch(request: Request): Promise<Response> {
    try {
      const url = new URL(request.url)
      const unauthorized = authorize(request, env)
      if (unauthorized) return unauthorized

      if (url.pathname === '/health') return handleHealth()
      if (url.pathname === '/api/placement/test' && request.method === 'GET') return handlePlacementTest(request, env)
      if (url.pathname === '/api/placement/assess' && request.method === 'POST') return handlePlacement(request, env)
      if (url.pathname === '/api/today-lesson' && request.method === 'GET') return handleTodayLesson(request, env)
      if (url.pathname === '/api/syllabus-lesson' && request.method === 'GET') return handleSyllabusModuleLesson(request, env)
      if (url.pathname === '/api/today-lesson/regenerate' && request.method === 'POST') return handleRegenerateTodayLesson(request, env)
      if (url.pathname === '/api/lesson/submit' && request.method === 'POST') return handleLessonSubmit(request, env)
      if (url.pathname === '/api/syllabus' && request.method === 'GET') return handleSyllabus(request, env)
      if (url.pathname === '/api/syllabus/regenerate' && request.method === 'POST') return handleRegenerateSyllabus(request, env)
      if (url.pathname === '/api/lessons/next' && request.method === 'POST') return handleNextLesson(request, env)
      if (url.pathname === '/api/writing/review' && request.method === 'POST') return handleWritingReview(request, env)
      if (url.pathname === '/api/progress/complete' && request.method === 'POST') return handleCompleteLesson(request, env)
      if (url.pathname === '/api/review/complete' && request.method === 'POST') return handleCompleteReview(request, env)
      if (url.pathname === '/api/me/summary' && request.method === 'GET') return handleSummary(request, env)
      return new Response('Not Found', { status: 404 })
    } catch (error) {
      console.error('Unhandled request error', error)
      return new Response(JSON.stringify({ error: 'Internal server error' }), {
        status: 500,
        headers: { 'content-type': 'application/json; charset=utf-8' },
      })
    }
  },
})
