import { createApp } from './app'
import type { Env } from './env'

export default {
  fetch(request: Request, env: Env): Response | Promise<Response> {
    return createApp(env).fetch(request)
  },
}
