import { describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ShowsListComponent } from './shows-list.component';
import { CatalogService } from '../../core/catalog.service';
import { ShowSummary } from '../../core/models';

registerLocaleData(localePt);

function show(partial: Partial<ShowSummary> & { id: string }): ShowSummary {
  return {
    name: 'Show',
    description: 'Desc',
    date: '2026-10-10T19:00:00-03:00',
    address: {
      street: 'Rua',
      number: 's/n',
      neighborhood: 'Centro',
      city: 'São Paulo',
      state: 'SP',
      country: 'Brasil',
      zipCode: '01001-000',
    },
    published: true,
    totalSpots: 100,
    totalSpotsSold: 10,
    partnerId: 'p1',
    ...partial,
  };
}

function setup(items: ShowSummary[]) {
  const listShows = vi.fn(() =>
    of({ currentPage: 0, perPage: 200, totalItems: items.length, items }),
  );
  TestBed.configureTestingModule({
    imports: [ShowsListComponent],
    providers: [
      provideRouter([]),
      { provide: CatalogService, useValue: { listShows } },
    ],
  });
  const fixture = TestBed.createComponent(ShowsListComponent);
  fixture.detectChanges();
  return fixture.componentInstance;
}

const items = [
  show({ id: '1', name: 'Rock na Praça', date: '2026-12-01T19:00:00-03:00' }),
  show({
    id: '2',
    name: 'Samba de Raiz',
    date: '2026-10-05T19:00:00-03:00',
    address: { street: 'Rua', number: 's/n', neighborhood: 'Centro', city: 'Rio de Janeiro', state: 'RJ', country: 'Brasil', zipCode: '20000-000' },
  }),
  show({ id: '3', name: 'Rascunho', published: false }),
];

describe('ShowsListComponent', () => {
  it('lista só publicados ordenados por data crescente', () => {
    const c = setup(items);
    expect(c.total()).toBe(2);
    expect(c.shows().map((s) => s.id)).toEqual(['2', '1']);
  });

  it('filtra por texto em todos os campos', () => {
    const c = setup(items);
    c.setQuery('rock');
    expect(c.shows().map((s) => s.id)).toEqual(['1']);
    c.setQuery('rio');
    expect(c.shows().map((s) => s.id)).toEqual(['2']);
  });

  it('restringe a busca ao escopo (nome/cidade/estado)', () => {
    const c = setup(items);
    c.setScope('city');
    c.setQuery('rock');
    expect(c.total()).toBe(0);
    c.setQuery('rio');
    expect(c.shows().map((s) => s.id)).toEqual(['2']);
    c.setScope('state');
    c.setQuery('rj');
    expect(c.shows().map((s) => s.id)).toEqual(['2']);
    c.setScope('name');
    c.setQuery('samba');
    expect(c.shows().map((s) => s.id)).toEqual(['2']);
  });

  it('ordena por nome', () => {
    const c = setup(items);
    c.setSort('name');
    expect(c.shows().map((s) => s.id)).toEqual(['1', '2']);
  });

  it('zera a página ao trocar filtro', () => {
    const c = setup(items);
    c.goTo(1);
    expect(c.page()).toBe(1);
    c.setScope('city');
    expect(c.page()).toBe(0);
  });
});
