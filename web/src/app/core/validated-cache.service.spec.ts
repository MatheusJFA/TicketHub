import { describe, expect, it } from 'vitest';
import { ValidatedCacheService } from './validated-cache.service';
import { ValidateTicketResponse } from './tickets.service';

function ticket(id: string): ValidateTicketResponse {
  return {
    showId: 's1',
    ticketId: id,
    code: 'c-' + id,
    orderId: 'o1',
    spotId: 'sp1',
    location: 'A1',
    showDate: '2026-10-10T19:00:00-03:00',
    checkedInAt: '2026-10-10T19:05:00-03:00',
  };
}

describe('ValidatedCacheService', () => {
  it('lembra e recupera por show + ticket', () => {
    const cache = new ValidatedCacheService();
    expect(cache.lookup('s1', 't1')).toBeNull();
    cache.remember('s1', ticket('t1'));
    expect(cache.lookup('s1', 't1')?.code).toBe('c-t1');
    // Outro show não enxerga
    expect(cache.lookup('s2', 't1')).toBeNull();
  });

  it('isola shows e limpa sob demanda', () => {
    const cache = new ValidatedCacheService();
    cache.remember('s1', ticket('t1'));
    cache.clear('s1');
    expect(cache.lookup('s1', 't1')).toBeNull();
  });

  it('não quebra sem localStorage (SSR)', () => {
    const cache = new ValidatedCacheService();
    cache.remember('s9', ticket('t9'));
    expect(cache.lookup('s9', 't9')?.ticketId).toBe('t9');
  });
});
