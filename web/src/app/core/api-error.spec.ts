import { describe, expect, it } from 'vitest';
import { parseApiError } from './api-error';

describe('parseApiError', () => {
  it('retorna a primeira mensagem do backend', () => {
    expect(
      parseApiError({ error: { errors: [{ message: 'Assento ocupado' }] } }, 'fallback'),
    ).toBe('Assento ocupado');
  });

  it('aceita message direto', () => {
    expect(parseApiError({ error: { message: 'Ops' } }, 'fallback')).toBe('Ops');
  });

  it('usa fallback quando não há mensagem', () => {
    expect(parseApiError({}, 'fallback')).toBe('fallback');
    expect(parseApiError(null, 'fallback')).toBe('fallback');
    expect(parseApiError({ error: { errors: [{ message: '  ' }] } }, 'fallback')).toBe(
      'fallback',
    );
  });
});
