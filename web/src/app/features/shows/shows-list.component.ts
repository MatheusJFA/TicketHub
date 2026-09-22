import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CatalogService } from '../../core/catalog.service';
import { ShowSummary } from '../../core/models';

@Component({
  selector: 'app-shows-list',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './shows-list.component.html',
})
export class ShowsListComponent {
  private readonly catalog = inject(CatalogService);

  readonly shows = signal<ShowSummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.catalog.listShows().subscribe({
      next: (page) => {
        this.shows.set(page.items.filter((show) => show.published));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar os shows. Verifique se o backend está no ar.');
        this.loading.set(false);
      },
    });
  }
}
