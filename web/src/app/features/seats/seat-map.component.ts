import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { CatalogService } from '../../core/catalog.service';
import { CartService } from '../../core/cart.service';
import { SectionSummary, ShowDetail, SpotItem } from '../../core/models';

interface SectionMap {
  section: SectionSummary;
  spots: SpotItem[];
}

@Component({
  selector: 'app-seat-map',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './seat-map.component.html',
})
export class SeatMapComponent {
  private readonly catalog = inject(CatalogService);
  readonly cart = inject(CartService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly show = signal<ShowDetail | null>(null);
  readonly map = signal<SectionMap[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    const id = this.route.snapshot.paramMap.get('id') ?? '';
    this.catalog
      .getShow(id)
      .pipe(
        switchMap((show) => {
          this.show.set(show);
          return this.catalog.listShowSections(id, 0, 20).pipe(
            map((page) => page.items.filter((section) => section.published)),
            switchMap((sections) => {
              if (sections.length === 0) {
                return of([]);
              }
              return forkJoin(
                sections.map((section) =>
                  this.catalog.listSectionSpots(section.id, 0, 100).pipe(
                    map((page) => ({
                      section,
                      spots: [...page.items]
                        .filter((spot) => spot.published)
                        .sort((a, b) =>
                          a.location.localeCompare(b.location, undefined, { numeric: true }),
                        ),
                    })),
                    catchError(() => of({ section, spots: [] })),
                  ),
                ),
              );
            }),
          );
        }),
      )
      .subscribe({
        next: (map) => {
          this.map.set(map);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Não foi possível carregar o mapa de assentos.');
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
