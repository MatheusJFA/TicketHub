import { describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { of, throwError } from 'rxjs';
import { CheckInComponent } from './check-in.component';
import { CatalogService } from '../../catalog/catalog.service';
import { TicketsService } from '../../tickets/tickets.service';

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

const GRANTED = {
  showId: 's1',
  ticketId: 't1',
  code: 'CODE1',
  orderId: 'o1',
  spotId: 'sp1',
  location: 'A1',
  showDate: '2026-10-10T19:00:00-03:00',
  checkedInAt: '2026-10-10T20:00:00-03:00',
};

function setup(overrides: { validate?: ReturnType<typeof vi.fn>; buyers?: ReturnType<typeof vi.fn> } = {}) {
  const validate = overrides.validate ?? vi.fn(() => of(GRANTED));
  const buyers = overrides.buyers ?? vi.fn(() => of([]));
  TestBed.configureTestingModule({
    imports: [CheckInComponent],
    providers: [
      {
        provide: CatalogService,
        useValue: {
          listShows: () =>
            of({ currentPage: 0, perPage: 50, totalItems: 1, items: [SHOW] }),
        },
      },
      { provide: TicketsService, useValue: { validate, buyers } },
    ],
  });
  const fixture = TestBed.createComponent(CheckInComponent);
  fixture.detectChanges();
  const component = fixture.componentInstance;
  component.onShowChange('s1');
  return { component, validate, buyers };
}

describe('CheckInComponent online', () => {
  it('libera a entrada quando a validação online passa', () => {
    const { component, validate } = setup();
    component.qr = 't1:CODE1:sig1';
    component.validate();
    expect(validate).toHaveBeenCalledWith('s1', 't1', 'CODE1', 'sig1');
    expect(component.state()).toBe('granted');
    expect(component.result()?.location).toBe('A1');
  });

  it('recusa QR malformado sem chamar o backend', () => {
    const { component, validate } = setup();
    component.qr = 't1:CODE1';
    component.validate();
    expect(validate).not.toHaveBeenCalled();
    expect(component.state()).toBe('denied');
  });

  it('recusa quando o backend retorna erro', () => {
    const { component } = setup({
      validate: vi.fn(() => throwError(() => ({ status: 422 }))),
    });
    component.qr = 't1:CODE1:sig1';
    component.validate();
    expect(component.state()).toBe('denied');
    expect(component.error()).toBeTruthy();
  });

  it('carrega os compradores do show', () => {
    const buyersList = [
      {
        ticketId: 't1',
        status: 'ISSUED',
        spotId: 'sp1',
        location: 'A1',
        buyerName: 'Ada',
        buyerCpf: '123',
        buyerEmail: 'ada@mail.com',
      },
    ];
    const { component, buyers } = setup({
      buyers: vi.fn(() => of(buyersList)),
    });
    component.loadBuyers();
    expect(buyers).toHaveBeenCalledWith('s1');
    expect(component.buyers()).toEqual(buyersList);
  });

  it('trocar de show limpa estado e compradores', () => {
    const { component } = setup();
    component.qr = 't1:CODE1:sig1';
    component.validate();
    expect(component.state()).toBe('granted');
    component.onShowChange('s1');
    expect(component.state()).toBe('idle');
    expect(component.buyers()).toEqual([]);
  });
});
