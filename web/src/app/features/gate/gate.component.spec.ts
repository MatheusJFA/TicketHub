import { describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { of } from 'rxjs';
import { GateComponent } from './gate.component';
import { CatalogService } from '../../core/catalog.service';
import { TicketsService } from '../../core/tickets.service';
import { ValidatedCacheService } from '../../core/validated-cache.service';

registerLocaleData(localePt);

const SHOW = {
  id: 's1',
  name: 'Show',
  description: 'd',
  date: '2026-10-10T19:00:00-03:00',
  address: {
    street: 'R',
    number: 's/n',
    neighborhood: 'C',
    city: 'SP',
    state: 'SP',
    country: 'BR',
    zipCode: '0',
  },
  published: true,
  totalSpots: 10,
  totalSpotsSold: 0,
  partnerId: 'p',
};

function setup() {
  const validate = vi.fn();
  TestBed.configureTestingModule({
    imports: [GateComponent],
    providers: [
      {
        provide: CatalogService,
        useValue: {
          listShows: () =>
            of({ currentPage: 0, perPage: 50, totalItems: 1, items: [SHOW] }),
        },
      },
      { provide: TicketsService, useValue: { validate } },
      ValidatedCacheService,
    ],
  });
  const fixture = TestBed.createComponent(GateComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  const cache = TestBed.inject(ValidatedCacheService);
  cache.savePreload('s1', [
    { ticketId: 't1', code: 'CODE1', signature: 'sig1', status: 'ISSUED', spotId: 'sp1', location: 'A1' },
    { ticketId: 't2', code: 'CODE2', signature: 'sig2', status: 'USED', spotId: 'sp2', location: 'A2' },
  ]);
  component.onShowChange('s1');
  component.online.set(false);
  return { component, cache, validate };
}

describe('GateComponent offline', () => {
  it('libera ingresso da pré-carga e enfileira o sync', () => {
    const { component } = setup();
    component.qr = 't1:CODE1:sig1';
    component.validate();
    expect(component.state()).toBe('granted');
    expect(component.offlineResult()).toBe(true);
    expect(component.pending().map((p) => p.ticketId)).toEqual(['t1']);
    expect(component.result()?.location).toBe('A1');
  });

  it('recusa par código divergente ou desconhecido', () => {
    const { component } = setup();
    component.qr = 't1:WRONG:sig1';
    component.validate();
    expect(component.state()).toBe('denied');
    component.qr = 'zz:CODE1:sig1';
    component.validate();
    expect(component.state()).toBe('denied');
  });

  it('recusa ingresso USED ou já usado localmente', () => {
    const { component } = setup();
    component.qr = 't2:CODE2:sig2';
    component.validate();
    expect(component.state()).toBe('denied');
    expect(component.error()).toContain('utiliz');
  });

  it('não libera o mesmo ingresso offline duas vezes', () => {
    const { component } = setup();
    component.qr = 't1:CODE1:sig1';
    component.validate();
    expect(component.state()).toBe('granted');
    component.qr = 't1:CODE1:sig1';
    component.validate();
    expect(component.state()).toBe('denied');
  });
});
