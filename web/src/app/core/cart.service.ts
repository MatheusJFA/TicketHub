import { Injectable, inject, signal } from '@angular/core';
import { SpotItem } from './models';

@Injectable({ providedIn: 'root' })
export class CartService {
  readonly spots = signal<SpotItem[]>([]);

  readonly ids = () => this.spots().map((spot) => spot.id);

  toggle(spot: SpotItem): void {
    if (!spot.available || !spot.published) {
      return;
    }
    const current = this.spots();
    this.spots.set(
      current.some((item) => item.id === spot.id)
        ? current.filter((item) => item.id !== spot.id)
        : [...current, spot],
    );
  }

  contains(id: string): boolean {
    return this.spots().some((item) => item.id === id);
  }

  clear(): void {
    this.spots.set([]);
  }
}
