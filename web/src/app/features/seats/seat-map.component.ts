import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { CatalogService } from '../../core/catalog.service';
import { CartService } from '../../core/cart.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ListErrorComponent } from '../../core/list-error.component';
import { SectionSummary, ShowDetail, SpotItem } from '../../core/models';

interface SectionMap {
  section: SectionSummary;
  spots: SpotItem[];
}

@Component({
  selector: 'app-seat-map',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ListErrorComponent],
  templateUrl: './seat-map.component.html',
})
export class SeatMapComponent {
  private readonly catalog = inject(CatalogService);
  readonly cart = inject(CartService);
  private readonly router = inject(Router);
  readonly i18n = inject(I18nService);

  readonly id = input<string>('');

  readonly show = signal<ShowDetail | null>(null);
  readonly map = signal<SectionMap[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  /** Linhas agrupadas memoizadas — evita recomputar O(spots) a cada CD. */
  readonly grouped = computed(() =>
    this.map().map((entry) => ({ sectionId: entry.section.id, rows: groupRows(entry) })),
  );
  /** Total do carrinho memoizado. */
  readonly total = computed(() => {
    let value = 0;
    let currency = 'BRL';
    const ids = new Set(this.cart.ids());
    for (const entry of this.map()) {
      currency = entry.section.price.currency || currency;
      for (const spot of entry.spots) {
        if (ids.has(spot.id)) value += entry.section.price.value;
      }
    }
    return { value, currency };
  });

  constructor() {
    effect(() => {
      const id = this.id();
      if (id) this.load(id);
    });
  }

  retry(): void {
    const id = this.id();
    if (id) this.load(id);
  }

  private load(id: string): void {
    this.loading.set(true);
    this.error.set(null);    this.catalog
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
          this.error.set(this.i18n.t('errors.mapFailed'));
          this.loading.set(false);
        },
      });
  }

  toggle(spot: SpotItem): void {
    this.cart.toggle(spot);
  }

  clear(): void {
    this.cart.clear();
  }

  /** Lookup barato no `grouped` memoizado (evita O(spots) por CD no template). */
  rowsFor(sectionId: string): { row: string; spots: SpotItem[] }[] {
    return this.grouped().find((g) => g.sectionId === sectionId)?.rows ?? [];
  }

  /** Legado: mantido p/ compat, prefira `grouped`/`total` (computed). */
  rows(entry: SectionMap): { row: string; spots: SpotItem[] }[] {
    return groupRows(entry);
  }

  /** Sums the current cart using each spot's section price. */
  cartTotal(): { value: number; currency: string } {
    return this.total();
  }

  checkout(): void {
    if (this.cart.spots().length > 0) {
      this.router.navigate(['/checkout']);
    }
  }
}

/** Groups a section's spots by row prefix (letters of "A12" → row "A"). */
function groupRows(entry: SectionMap): { row: string; spots: SpotItem[] }[] {
  const groups = new Map<string, SpotItem[]>();
  for (const spot of entry.spots) {
    const prefix = (/^[A-Za-z]+/.exec(spot.location)?.[0] ?? '#').toUpperCase();
    const list = groups.get(prefix) ?? [];
    list.push(spot);
    groups.set(prefix, list);
  }
  return [...groups.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([row, spots]) => ({ row, spots }));
}
