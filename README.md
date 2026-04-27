# CEFR English App

- `worker/`: Cloudflare Workers + D1 backend
- `android/`: Kotlin Android client
- `docs/`: 规格、实现计划与开发记录

## Worker local run
1. Install dependencies: `npm --prefix /home/wenha/project/AndroidWork/cefr-english-app/worker install`
2. Apply D1 migrations: `npx wrangler d1 migrations apply cefr-english-db --local --config /home/wenha/project/AndroidWork/cefr-english-app/worker/wrangler.jsonc`
3. Start Worker: `npx wrangler dev --config /home/wenha/project/AndroidWork/cefr-english-app/worker/wrangler.jsonc`

## Manual smoke test
1. Call `POST /api/placement/assess`
2. Call `GET /api/today-lesson?userId=u1`
3. Optional: call `POST /api/today-lesson/regenerate`
4. Call `POST /api/lesson/submit`
5. Call `GET /api/me/summary?userId=u1`

## Android local verification
1. Compile: `env GRADLE_USER_HOME=/tmp/gradle-home gradle -p /home/wenha/project/AndroidWork/cefr-english-app/android :app:compileDebugKotlin --no-daemon --console=plain`
2. Unit tests: `env GRADLE_USER_HOME=/tmp/gradle-home gradle -p /home/wenha/project/AndroidWork/cefr-english-app/android testDebugUnitTest --no-daemon --console=plain`

## Worker verification
Run: `node /home/wenha/project/AndroidWork/cefr-english-app/worker/node_modules/vitest/vitest.mjs run /home/wenha/project/AndroidWork/cefr-english-app/worker/test`

## Current learning flow
1. First use: run placement once to determine the starting CEFR level.
2. Home screen: load the current daily lesson and summary.
3. Lesson screen: complete reading, grammar, and short writing.
4. Submit the lesson: receive writing feedback and advance to the next lesson.
5. Progress screen: inspect current level, current lesson, completed count, and today status.

## References
- Spec: `docs/superpowers/specs/2026-04-27-cefr-daily-lesson-design.md`
- Plan: `docs/superpowers/plans/2026-04-27-cefr-daily-lesson-implementation.md`
- Dev record: `docs/superpowers/records/2026-04-27-cefr-daily-lesson-dev-record.md`

## Cloudflare deployment note
Before creating D1 or deploying the Worker, authenticate Wrangler first:
`npm --prefix "/home/wenha/project/AndroidWork/cefr-english-app/worker" exec wrangler -- login`
