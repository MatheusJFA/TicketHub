import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CatalogService } from '../../core/catalog.service';
import { CartService } from '../../core/cart.service';
import { SpotItem } from '../../core/models';

@Component({
  selector: 'app-seats',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './seats.component.html',
})
export class SeatsComponent {
  private readonly catalog = inject(CatalogService);
  readonly cart = inject(CartService);
  private readonly router = inject(Router);

  search = '';
  readonly spots = signal<SpotItem[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.catalog.listSpots(this.search).subscribe({
      next: (page) => {
        this.spots.set(page.items.filter((spot) => spot.published));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar os assentos.');
        this.loading.set(false);
      },
    });
  }

  toggle(spot: SpotItem): void {
    this.cart.toggle(spot);
  }

  checkout(): void {
    if (this.cart.spots().length > 0) {
      this.router.navigate(['/checkout']);
    }
  }
}
