import { describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { CartService } from './cart.service';
import { SpotItem } from './models';

function spot(id: string, location = 'A1'): SpotItem {
  return {
    id,
    location,
    available: true,
    published: true,
    createdAt: '',
    updatedAt: '',
    deletedAt: null,
  };
}

describe('CartService', () => {
  it('alterna assentos e expõe ids como computed', () => {
    TestBed.configureTestingModule({});
    const cart = TestBed.inject(CartService);

    cart.toggle(spot('1'));
    cart.toggle(spot('2', 'A2'));
    expect(cart.ids()).toEqual(['1', '2']);

    cart.toggle(spot('1'));
    expect(cart.ids()).toEqual(['2']);
    expect(cart.contains('2')).toBe(true);
  });

  it('ignora assento indisponível ou não publicado', () => {
    TestBed.configureTestingModule({});
    const cart = TestBed.inject(CartService);

    cart.toggle({ ...spot('x'), available: false });
    cart.toggle({ ...spot('y'), published: false });
    expect(cart.spots()).toEqual([]);
  });
});
