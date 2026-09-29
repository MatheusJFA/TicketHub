import { describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { PaginationComponent } from './pagination.component';

function setup(page: number, total: number, perPage = 12) {
  TestBed.configureTestingModule({ imports: [PaginationComponent] });
  const fixture = TestBed.createComponent(PaginationComponent);
  fixture.componentRef.setInput('page', page);
  fixture.componentRef.setInput('total', total);
  fixture.componentRef.setInput('perPage', perPage);
  fixture.detectChanges();
  return fixture.componentInstance;
}

describe('PaginationComponent', () => {
  it('lista todas as páginas quando poucas', () => {
    const c = setup(0, 37);
    expect(c.totalPages()).toBe(4);
    expect(c.items()).toEqual([0, 1, 2, 3]);
    expect(c.from()).toBe(1);
    expect(c.to()).toBe(12);
  });

  it('usa reticências com muitas páginas', () => {
    const c = setup(5, 200);
    expect(c.items()).toEqual([0, '…', 4, 5, 6, '…', 16]);
  });

  it('calcula o intervalo da última página', () => {
    const c = setup(3, 37);
    expect(c.from()).toBe(37);
    expect(c.to()).toBe(37);
  });

  it('ignora destino inválido ou igual', () => {
    const c = setup(1, 37);
    const emitted: number[] = [];
    c.pageChange.subscribe((p) => emitted.push(p));
    c.go(1);
    c.go(-1);
    c.go(99);
    expect(emitted).toEqual([]);
    c.go(2);
    expect(emitted).toEqual([2]);
  });
});
