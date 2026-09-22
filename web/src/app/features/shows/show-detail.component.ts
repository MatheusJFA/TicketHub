import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CatalogService } from '../../core/catalog.service';
import { ShowDetail } from '../../core/models';

@Component({
  selector: 'app-show-detail',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './show-detail.component.html',
})
export class ShowDetailComponent {
  private readonly catalog = inject(CatalogService);
  private readonly route = inject(ActivatedRoute);

  readonly show = signal<ShowDetail | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    const id = this.route.snapshot.paramMap.get('id') ?? '';
    this.catalog.getShow(id).subscribe({
      next: (show) => {
        this.show.set(show);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Show não encontrado.');
        this.loading.set(false);
      },
    });
  }
}
