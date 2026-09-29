import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { CatalogService } from '../../catalog/catalog.service';
import { I18nService } from '../../shared/i18n/i18n.service';
import { ListErrorComponent } from '../../shared/ui/list-error.component';
import { TicketsService, BuyerTicket, ValidateTicketResponse } from '../../tickets/tickets.service';
import { parseApiError } from '../../core/api-error';
import { IconComponent } from '../../shared/ui/icon.component';
import { ShowSummary } from '../../core/models';

type GateState = 'idle' | 'checking' | 'granted' | 'denied';

@Component({
  selector: 'app-check-in',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe, ListErrorComponent, IconComponent],
  templateUrl: './check-in.component.html',
})
export class CheckInComponent {
  private readonly catalog = inject(CatalogService);
  private readonly tickets = inject(TicketsService);
  readonly i18n = inject(I18nService);

  readonly shows = signal<ShowSummary[]>([]);
  showId = '';
  qr = '';
  readonly state = signal<GateState>('idle');
  readonly result = signal<ValidateTicketResponse | null>(null);
  readonly error = signal<string | null>(null);
  /** Lista de compradores do show selecionado (nome, CPF, e-mail). */
  readonly buyers = signal<BuyerTicket[]>([]);
  readonly buyersLoading = signal(false);

  constructor() {
    this.catalog.listShows('', 0, 50).subscribe({
      next: (page) => {
        this.shows.set(page.items);
        const published = page.items.find((s) => s.published);
        if (published) {
          this.showId = published.id;
        }
      },
      error: () => this.error.set(this.i18n.t('errors.loadShows')),
    });
  }

  onShowChange(id: string): void {
    this.showId = id;
    this.reset();
    this.buyers.set([]);
  }

  loadBuyers(): void {
    if (!this.showId || this.buyersLoading()) return;
    this.buyersLoading.set(true);
    this.tickets.buyers(this.showId).subscribe({
      next: (buyers) => {
        this.buyers.set(buyers);
        this.buyersLoading.set(false);
      },
      error: (err) => {
        this.buyersLoading.set(false);
        this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
      },
    });
  }

  validate(): void {
    this.error.set(null);
    this.result.set(null);
    const parts = this.qr.trim().split(':');
    if (!this.showId || parts.length !== 3 || parts.some((p) => !p)) {
      this.state.set('denied');
      return;
    }
    this.state.set('checking');
    this.tickets.validate(this.showId, parts[0], parts[1], parts[2]).subscribe({
      next: (res) => {
        this.result.set(res);
        this.state.set('granted');
      },
      error: (err) => {
        this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
        this.state.set('denied');
      },
    });
  }

  reset(): void {
    this.qr = '';
    this.state.set('idle');
    this.result.set(null);
    this.error.set(null);
  }
}
