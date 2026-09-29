import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, of, from, mergeMap } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { CatalogService } from '../../catalog/catalog.service';
import { AdminService } from '../../admin/admin.service';
import { ConfirmService } from '../../shared/ui/confirm.service';
import { I18nService } from '../../shared/i18n/i18n.service';
import { SectionSummary, ShowDetail, SpotItem } from '../../core/models';
import { parseApiError } from '../../core/api-error';
import { IconComponent } from '../../shared/ui/icon.component';

interface SectionMap {
  section: SectionSummary;
  spots: SpotItem[];
}

interface BuildProgress {
  done: number;
  total: number;
}

@Component({
  selector: 'app-admin-show-detail',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink, DecimalPipe, IconComponent],
  templateUrl: './admin-show-detail.component.html',
})
export class AdminShowDetailComponent {
  private readonly catalog = inject(CatalogService);
  private readonly admin = inject(AdminService);
  private readonly confirm = inject(ConfirmService);
  readonly i18n = inject(I18nService);

  readonly id = input<string>('');
  readonly showId$ = input<string>('', { alias: 'showId' });

  readonly show = signal<ShowDetail | null>(null);
  readonly map = signal<SectionMap[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  // map builder form
  sectionName = '';
  sectionDescription = '';
  rowStart = 'A';
  rowCount = 3;
  seatsPerRow = 10;
  sectionPrice = 50;
  readonly building = signal<BuildProgress | null>(null);

  // inline section editing
  editingSection: Record<string, { name: string; price: number }> = {};
  newSpotLocation: Record<string, string> = {};

  constructor() {
    effect(() => {
      if (this.showId) this.reload();
    });
  }

  private get showId(): string {
    return this.id() || this.showId$();
  }

  /** Preview rows derived from the builder form (no backend calls). */
  previewRows(): { row: string; seats: string[] }[] {
    const start = this.rowStart.trim().toUpperCase();
    if (!/^[A-Z]$/.test(start)) return [];
    const rows = Math.max(1, Math.min(26, Math.floor(this.rowCount) || 0));
    const seats = Math.max(1, Math.min(100, Math.floor(this.seatsPerRow) || 0));
    const base = start.charCodeAt(0);
    const out: { row: string; seats: string[] }[] = [];
    for (let r = 0; r < rows && base + r <= 90; r++) {
      const row = String.fromCharCode(base + r);
      out.push({ row, seats: Array.from({ length: seats }, (_, i) => `${row}${i + 1}`) });
    }
    return out;
  }

  previewTotal(): number {
    return this.previewRows().reduce((acc, r) => acc + r.seats.length, 0);
  }

  reload(): void {
    this.loading.set(true);
    this.catalog
      .getShow(this.showId)
      .pipe(
        switchMap((show) => {
          this.show.set(show);
          return this.catalog.listShowSections(this.showId, 0, 50).pipe(
            switchMap((page) => {
              if (page.items.length === 0) {
                return of([]);
              }
              return forkJoin(
                page.items.map((section) =>
                  this.catalog.listAllSectionSpots(section.id).pipe(
                    map((spots) => ({ section, spots })),
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
          this.error.set(this.i18n.t('errors.loadShow'));
          this.loading.set(false);
        },
      });
  }

  /** Creates a section and labels every generated spot as ROW+NUMBER (A1, A2…). */
  buildSection(): void {
    const rows = this.previewRows();
    if (!this.sectionName.trim() || rows.length === 0) return;
    const labels = rows.flatMap((r) => r.seats);
    this.error.set(null);
    this.building.set({ done: 0, total: labels.length });

    // POST /shows/{id}/sections answers with the SHOW id, so diff the list.
    const beforeIds = new Set(this.map().map((e) => e.section.id));
    this.admin
      .addSection(this.showId, {
        name: this.sectionName.trim(),
        description: this.sectionDescription.trim(),
        totalSpots: labels.length,
        price: { value: this.sectionPrice, currency: 'BRL' },
      })
      .pipe(
        switchMap(() => this.catalog.listShowSections(this.showId, 0, 50)),
        map((page) => {
          const created = page.items.find((s) => !beforeIds.has(s.id));
          if (!created) throw new Error('section not found after create');
          return created;
        }),
        switchMap((section) => this.catalog.listAllSectionSpots(section.id).pipe(
          map((spots) => ({ section, spots })),
        )),
        switchMap(({ section, spots }) => {
          const ordered = [...spots].sort((a, b) => a.createdAt.localeCompare(b.createdAt));
          const pairs = ordered.slice(0, labels.length).map((spot, i) => ({ spot, label: labels[i]! }));
          return from(pairs).pipe(
            // Paralelismo limitado: 200 PATCHs seriais (~minutos) viram ~30s com 6 em voo.
            mergeMap(
              ({ spot, label }) =>
                this.admin.renameSpot(spot.id, label).pipe(
                  tap(() => this.building.update((p) => (p ? { done: p.done + 1, total: p.total } : p))),
                  catchError(() => of(null)),
                ),
              6,
            ),
            map(() => section),
          );
        }),
      )
      .subscribe({
        next: () => {
          this.building.set(null);
          this.sectionName = '';
          this.sectionDescription = '';
          this.reload();
        },
        error: (err) => {
          this.building.set(null);
          this.error.set(parseApiError(err, this.i18n.t('errors.sectionCreateFailed')));
          this.reload();
        },
      });
  }

  publishAllSection(id: string): void {
    this.admin.publishAllSection(id).subscribe({
      next: () => this.reload(),
      error: (err) => this.error.set(parseApiError(err, this.i18n.t('errors.sectionPublishFailed'))),
    });
  }

  unpublishAllSection(id: string): void {
    this.admin.unpublishAllSection(id).subscribe({
      next: () => this.reload(),
      error: (err) => this.error.set(parseApiError(err, this.i18n.t('errors.sectionPublishFailed'))),
    });
  }

  deleteSection(id: string): void {
    void this.confirm.ask(this.i18n.t('admin.mapConfirmDelete')).then((ok) => {
      if (!ok) return;
      this.admin.deleteSection(id).subscribe({
        next: () => this.reload(),
        error: (err) => this.error.set(parseApiError(err, this.i18n.t('errors.generic'))),
      });
    });
  }

  startEditSection(section: SectionSummary): void {
    this.editingSection[section.id] = { name: section.name, price: section.price.value };
  }

  saveSection(section: SectionSummary): void {
    const edit = this.editingSection[section.id];
    if (!edit) return;
    this.admin
      .renameSection(section.id, edit.name)
      .pipe(switchMap(() => this.admin.changeSectionPrice(section.id, edit.price)))
      .subscribe({
        next: () => {
          delete this.editingSection[section.id];
          this.reload();
        },
        error: (err) => this.error.set(parseApiError(err, this.i18n.t('errors.generic'))),
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
      error: (err) => this.error.set(parseApiError(err, this.i18n.t('errors.spotCreateFailed'))),
    });
  }

  deleteSpot(id: string): void {
    this.admin.deleteSpot(id).subscribe({
      next: () => this.reload(),
      error: (err) => this.error.set(parseApiError(err, this.i18n.t('errors.generic'))),
    });
  }

  publishSpot(id: string): void {
    this.admin.publishSpot(id).subscribe({
      next: () => this.reload(),
      error: (err) => this.error.set(parseApiError(err, this.i18n.t('errors.spotPublishFailed'))),
    });
  }
}
