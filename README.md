# CEFR English App

- `worker/`: Cloudflare Workers + D1 backend
- `android/`: Kotlin Android client
- `docs/`: 规格、实现计划与开发记录

## Worker local run
1. Install dependencies: `npm --prefix /home/wenha/project/AndroidWork/cefr-english-app/worker install`
2. Apply D1 migrations: `npx wrangler d1 migrations apply cefr-english-db --local --config /home/wenha/project/AndroidWork/cefr-english-app/worker/wrangler.jsonc`
3. Start Worker: `npx wrangler dev --config /home/wenha/project/AndroidWork/cefr-english-app/worker/wrangler.jsonc`

## New machine setup

Use this checklist when moving development to another computer.

### Required tools

- JDK 17
- Android Studio or Android SDK command line tools
- Android SDK Platform 35 and Build Tools
- Node.js 20 or newer
- npm
- Wrangler CLI, installed through the Worker package scripts or `npm exec`
- Gradle 9.x, or use the checked-in Android Gradle Wrapper

### Android setup

1. Clone the repository.
2. Install Android SDK Platform 35.
3. Make sure `ANDROID_HOME` points to the Android SDK directory.
4. Add `platform-tools` to `PATH` so `adb` is available.
5. Configure the Android API token locally. Put the same value as the Worker `API_TOKEN` secret into one of:
   - `android/gradle.properties`: `CEFR_API_TOKEN=...`
   - environment variable: `CEFR_API_TOKEN=...`
6. Build with either command:
   - `gradle -p android :app:assembleDebug --no-daemon`
   - `./gradlew -p android :app:assembleDebug --no-daemon`

If the wrapper repeatedly downloads Gradle, install a local Gradle 9.x and use the `gradle -p android ...` form.

### Worker setup

1. Install dependencies: `npm --prefix worker install`
2. Login to Cloudflare: `npm --prefix worker exec wrangler -- login`
3. For local development, apply local D1 migrations:
   `npm --prefix worker exec wrangler d1 migrations apply cefr-english-db --local --config worker/wrangler.jsonc`
4. Start local Worker:
   `npm --prefix worker exec wrangler dev --config worker/wrangler.jsonc`

### Secrets and deployment

The Android `CEFR_API_TOKEN` and Worker `API_TOKEN` must match. Do not commit either value.

Worker production also needs provider secrets configured in Cloudflare, such as the AI provider key. Set secrets with:

`npm --prefix worker exec wrangler secret put API_TOKEN --config worker/wrangler.jsonc`

Use the Cloudflare dashboard or Wrangler to copy existing production secrets when moving to a new computer.

### Verification

- Android compile: `gradle -p android :app:compileDebugKotlin --no-daemon`
- Android unit tests: `gradle -p android :app:testDebugUnitTest --no-daemon`
- Worker tests: `npm --prefix worker test`
- Device install: `adb install -r android/app/build/outputs/apk/debug/app-debug.apk`

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
