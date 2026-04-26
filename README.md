# CEFR English App

- `worker/`: Cloudflare Workers + D1 backend
- `android/`: Kotlin Android client

## Worker local run
1. Install dependencies: `npm --prefix /home/wenha/project/cefr-english-app/worker install`
2. Apply D1 migrations: `npx wrangler d1 migrations apply cefr-english-db --local --config /home/wenha/project/cefr-english-app/worker/wrangler.jsonc`
3. Start Worker: `npx wrangler dev --config /home/wenha/project/cefr-english-app/worker/wrangler.jsonc`

## Manual smoke test
1. Call `POST /api/placement/assess`
2. Call `POST /api/lessons/next`
3. Call `POST /api/writing/review`
4. Call `POST /api/progress/complete`
5. Call `GET /api/me/summary?userId=u1`

## Cloudflare deployment note
Before creating D1 or deploying the Worker, authenticate Wrangler first:
`npm --prefix "/home/wenha/project/cefr-english-app/worker" exec wrangler -- login`
