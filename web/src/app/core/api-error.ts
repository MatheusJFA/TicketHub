import { ApiError } from './models';

/** Extrai a mensagem do backend (`{ errors: [{ message }] }`) com fallback. */
export function parseApiError(err: unknown, fallback: string): string {
  const api = (err as { error?: ApiError })?.error;
  const first = api?.errors?.[0]?.message?.trim();
  if (first) return first;
  const msg = api?.message?.trim();
  if (msg) return msg;
  return fallback;
}
