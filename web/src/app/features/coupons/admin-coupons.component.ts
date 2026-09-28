import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CatalogService } from '../../core/catalog.service';
import { CouponsService } from '../../core/coupons.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ShowSummary } from '../../core/models';
import { parseApiError } from '../../core/api-error';

@Component({
  selector: 'app-admin-coupons',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-coupons.component.html',
})
export class AdminCouponsComponent {
  private readonly catalog = inject(CatalogService);
  private readonly coupons = inject(CouponsService);
  readonly i18n = inject(I18nService);

  readonly shows = signal<ShowSummary[]>([]);
  readonly creating = signal(false);
  readonly error = signal<string | null>(null);
  readonly createdId = signal<string | null>(null);

  code = '';
  kind: 'PERCENT' | 'FIXED' = 'PERCENT';
  showId = '';
  value: number | null = 10;
  validFrom = '';
  validUntil = '';
  maxUses: number | null = null;

  constructor() {
    this.catalog.listShows('', 0, 50).subscribe({
      next: (page) => this.shows.set(page.items),
      error: () => this.error.set(this.i18n.t('errors.loadShows')),
    });
  }

  submit(): void {
    this.error.set(null);
    this.createdId.set(null);
    this.creating.set(true);
    const toInstant = (v: string) => (v ? new Date(v).toISOString() : null);
    this.coupons
      .create({
        code: this.code.trim(),
        showId: this.showId || null,
        sectionId: null,
        kind: this.kind,
        percent: this.kind === 'PERCENT' ? this.value : null,
        fixedValue: this.kind === 'FIXED' ? this.value : null,
        fixedCurrency: this.kind === 'FIXED' ? 'BRL' : null,
        validFrom: toInstant(this.validFrom),
        validUntil: toInstant(this.validUntil),
        maxUses: this.maxUses,
      })
      .subscribe({
        next: (res) => {
          this.creating.set(false);
          this.createdId.set(res.id);
          this.code = '';
        },
        error: (err) => {
          this.creating.set(false);
          this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
        },
      });
  }
}
