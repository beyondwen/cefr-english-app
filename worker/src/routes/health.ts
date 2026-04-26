import { json } from '../lib/json'

export const handleHealth = (): Response => json({ status: 'ok' })
