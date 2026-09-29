import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { I18nService } from '../i18n/i18n.service';

export type PageItem = number | '…';

/**
 * Paginação numerada: primeira/última, janela ao redor da atual e
 * "Mostrando X–Y de Z". Página é 0-based; `pageChange` emite o destino.
 */
@Component({
  selector: 'app-pagination',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (totalPages() > 1) {
      <nav class="mt-6 flex flex-wrap items-center justify-center gap-1.5" [attr.aria-label]="i18n.t('pagination.label')">
        <button type="button" (click)="go(0)" [disabled]="page() === 0"
          class="th-btn-ghost !px-3 !py-1.5 !text-xs disabled:opacity-40" [attr.aria-label]="i18n.t('pagination.first')">«</button>
        <button type="button" (click)="go(page() - 1)" [disabled]="page() === 0"
          class="th-btn-ghost !px-3 !py-1.5 !text-xs disabled:opacity-40" [attr.aria-label]="i18n.t('pagination.prev')">←</button>
        @for (item of items(); track $index) {
          @if (item === '…') {
            <span class="px-1 text-sm font-bold text-[#6f6353]" aria-hidden="true">…</span>
          } @else {
            <button type="button" (click)="go(item)"
              [attr.aria-current]="item === page() ? 'page' : null"
              [class]="item === page()
                ? 'rounded-full border-[1.5px] border-[#2a241a] bg-[#2a241a] px-3.5 py-1.5 text-xs font-bold text-[#fdf8ec]'
                : 'th-btn-ghost !px-3.5 !py-1.5 !text-xs'">
              {{ item + 1 }}
            </button>
          }
        }
        <button type="button" (click)="go(page() + 1)" [disabled]="page() >= totalPages() - 1"
          class="th-btn-ghost !px-3 !py-1.5 !text-xs disabled:opacity-40" [attr.aria-label]="i18n.t('pagination.next')">→</button>
        <button type="button" (click)="go(totalPages() - 1)" [disabled]="page() >= totalPages() - 1"
          class="th-btn-ghost !px-3 !py-1.5 !text-xs disabled:opacity-40" [attr.aria-label]="i18n.t('pagination.last')">»</button>
      </nav>
      <p class="mt-2 text-center text-xs font-semibold text-[#6f6353]">
        {{ i18n.t('pagination.showing', { from: from(), to: to(), total: total() }) }}
      </p>
    }
  `,
})
export class PaginationComponent {
  readonly i18n = inject(I18nService);
  readonly page = input(0);
  readonly total = input(0);
  readonly perPage = input(12);
  readonly pageChange = output<number>();

  readonly totalPages = computed(() => Math.ceil(this.total() / this.perPage()));
  readonly from = computed(() =>
    this.total() === 0 ? 0 : this.page() * this.perPage() + 1,
  );
  readonly to = computed(() => Math.min((this.page() + 1) * this.perPage(), this.total()));

  /** Janela: primeira, atual±1, última, com reticências. */
  readonly items = computed<PageItem[]>(() => {
    const total = this.totalPages();
    const current = this.page();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    const set = new Set<number>([0, total - 1, current - 1, current, current + 1]);
    const pages = [...set].filter((p) => p >= 0 && p < total).sort((a, b) => a - b);
    const out: PageItem[] = [];
    pages.forEach((p, i) => {
      if (i > 0 && p - pages[i - 1] > 1) out.push('…');
      out.push(p);
    });
    return out;
  });

  go(target: number): void {
    if (target >= 0 && target < this.totalPages() && target !== this.page()) {
      this.pageChange.emit(target);
    }
  }
}
