import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { CatalogService } from '../../core/catalog.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ListErrorComponent } from '../../core/list-error.component';
import { PaginationComponent } from '../../core/pagination.component';
import { parseApiError } from '../../core/api-error';
import { ShowSummary } from '../../core/models';

export type CatalogSort = 'date-asc' | 'date-desc' | 'name';
export type CatalogScope = 'all' | 'name' | 'city' | 'state';

@Component({
  selector: 'app-shows-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, FormsModule, ListErrorComponent, PaginationComponent],
  templateUrl: './shows-list.component.html',
})
export class ShowsListComponent {
  private readonly catalog = inject(CatalogService);
  readonly i18n = inject(I18nService);

  readonly perPage = 12;
  readonly page = signal(0);
  readonly q = signal('');
  readonly scope = signal<CatalogScope>('all');
  readonly sort = signal<CatalogSort>('date-asc');

  /**
   * Catálogo cabe inteiro na memória (~dezenas de shows): busca/filtros/
   * ordenação rodam client-side e a paginação continua honesta. O backend
   * limita perPage a 100 — se o catálogo passar disso, buscar em páginas
   * ou voltar a paginar/ordenar no servidor (a API suporta
   * search/page/perPage/sort=name).
   */
  private readonly resource = rxResource({
    stream: () => this.catalog.listShows('', 0, 100),
  });

  private readonly all = computed(
    () => this.resource.value()?.items.filter((show) => show.published) ?? [],
  );

  readonly filtered = computed(() => {
    const term = this.q().trim().toLowerCase();
    const scope = this.scope();
    const list = this.all().filter((show) => {
      if (!term) return true;
      const name = show.name.toLowerCase();
      const desc = show.description.toLowerCase();
      const city = show.address.city.toLowerCase();
      const state = show.address.state.toLowerCase();
      switch (scope) {
        case 'name':
          return name.includes(term);
        case 'city':
          return city.includes(term);
        case 'state':
          return state.includes(term);
        default:
          return (
            name.includes(term) || desc.includes(term) || city.includes(term)
          );
      }
    });
    const byName = (a: ShowSummary, b: ShowSummary) => a.name.localeCompare(b.name);
    const byDate = (a: ShowSummary, b: ShowSummary) => a.date.localeCompare(b.date);
    return [...list].sort((a, b) => {
      switch (this.sort()) {
        case 'name':
          return byName(a, b);
        case 'date-desc':
          return byDate(b, a);
        default:
          return byDate(a, b);
      }
    });
  });

  readonly total = computed(() => this.filtered().length);
  readonly shows = computed(() => {
    const start = this.page() * this.perPage;
    return this.filtered().slice(start, start + this.perPage);
  });
  readonly loading = this.resource.isLoading;
  readonly error = computed(() => {
    const err = this.resource.error();
    return err ? parseApiError(err, this.i18n.t('errors.backendDown')) : null;
  });

  setQuery(value: string): void {
    this.q.set(value);
    this.page.set(0);
  }

  setScope(value: CatalogScope): void {
    this.scope.set(value);
    this.page.set(0);
  }

  setSort(value: CatalogSort): void {
    this.sort.set(value);
    this.page.set(0);
  }

  clearFilters(): void {
    this.q.set('');
    this.scope.set('all');
    this.sort.set('date-asc');
    this.page.set(0);
  }

  get hasActiveFilters(): boolean {
    return (
      this.q().trim() !== '' ||
      this.scope() !== 'all' ||
      this.sort() !== 'date-asc'
    );
  }

  reload(): void {
    this.resource.reload();
  }

  goTo(target: number): void {
    this.page.set(target);
  }

  occupancy(show: ShowSummary): number {
    if (!show.totalSpots) return 0;
    return Math.min(100, Math.round((show.totalSpotsSold / show.totalSpots) * 100));
  }
}
