import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { CatalogService } from '../../core/catalog.service';
import { AdminService } from '../../core/admin.service';
import { SectionSummary, ShowDetail, SpotItem } from '../../core/models';

interface SectionMap {
  section: SectionSummary;
  spots: SpotItem[];
}

@Component({
  selector: 'app-admin-show-detail',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-show-detail.component.html',
})
export class AdminShowDetailComponent {
  private readonly catalog = inject(CatalogService);
  private readonly admin = inject(AdminService);
  private readonly route = inject(ActivatedRoute);

  readonly showId = this.route.snapshot.paramMap.get('id') ?? '';
  readonly show = signal<ShowDetail | null>(null);
  readonly map = signal<SectionMap[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  sectionName = '';
  sectionDescription = '';
  sectionSpots = 10;
  sectionPrice = 50;
  newSpotLocation: Record<string, string> = {};

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.catalog
      .getShow(this.showId)
      .pipe(
        switchMap((show) => {
          this.show.set(show);
          return this.catalog.listShowSections(this.showId, 0, 20).pipe(
            switchMap((page) => {
              if (page.items.length === 0) {
                return of([]);
              }
              return forkJoin(
                page.items.map((section) =>
                  this.catalog.listSectionSpots(section.id, 0, 100).pipe(
                    map((spots) => ({ section, spots: spots.items })),
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
          this.error.set('Não foi possível carregar o show.');
          this.loading.set(false);
        },
      });
  }

  addSection(): void {
    this.error.set(null);
    this.admin
      .addSection(this.showId, {
        name: this.sectionName,
        description: this.sectionDescription,
        totalSpots: this.sectionSpots,
        price: { value: this.sectionPrice, currency: 'BRL' },
      })
      .subscribe({
        next: () => {
          this.sectionName = '';
          this.sectionDescription = '';
          this.reload();
        },
        error: (err) => this.error.set(err?.error?.errors?.[0]?.message ?? 'Falha ao criar setor.'),
      });
  }

  publishSection(id: string): void {
    this.admin.publishSection(id).subscribe({
      next: () => this.reload(),
      error: (err) => this.error.set(err?.error?.errors?.[0]?.message ?? 'Falha ao publicar setor.'),
    });
  }

  createSpot(sectionId: string): void {
    const location = (this.newSpotLocation[sectionId] ?? '').trim();
    if (!location) {
      return;
    }
    this.admin.createSpot(sectionId, location).subscribe({
      next: () => {
        this.newSpotLocation[sectionId] = '';
        this.reload();
      },
      error: (err) => this.error.set(err?.error?.errors?.[0]?.message ?? 'Falha ao criar assento.'),
    });
  }

  publishSpot(id: string): void {
    this.admin.publishSpot(id).subscribe({
      next: () => this.reload(),
      error: (err) => this.error.set(err?.error?.errors?.[0]?.message ?? 'Falha ao publicar assento.'),
    });
  }
}
