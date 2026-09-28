import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { fromEvent, merge, forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CatalogService } from '../../core/catalog.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ListErrorComponent } from '../../core/list-error.component';
import { TicketsService, ValidateTicketResponse } from '../../core/tickets.service';
import { ToastService } from '../../core/toast.service';
import {
  PendingCheckIn,
  PreloadStore,
  ValidatedCacheService,
} from '../../core/validated-cache.service';
import { parseApiError } from '../../core/api-error';
import { ShowSummary } from '../../core/models';

type GateState = 'idle' | 'checking' | 'granted' | 'denied';

@Component({
  selector: 'app-gate',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe, ListErrorComponent],
  templateUrl: './gate.component.html',
})
export class GateComponent {
  private readonly catalog = inject(CatalogService);
  private readonly tickets = inject(TicketsService);
  private readonly cache = inject(ValidatedCacheService);
  private readonly toast = inject(ToastService);
  readonly i18n = inject(I18nService);

  readonly shows = signal<ShowSummary[]>([]);
  showId = '';
  qr = '';
  readonly state = signal<GateState>('idle');
  readonly result = signal<ValidateTicketResponse | null>(null);
  readonly error = signal<string | null>(null);
  /** Verdadeiro quando o resultado veio do cache local (offline). */
  readonly offlineResult = signal(false);
  readonly online = signal(
    typeof navigator === 'undefined' ? true : navigator.onLine,
  );
  /** Pré-carga offline do show selecionado + fila de sync. */
  readonly preload = signal<PreloadStore | null>(null);
  readonly preloadCount = computed(() =>
    this.preload() ? Object.keys(this.preload()!.entries).length : 0,
  );
  readonly preloading = signal(false);
  readonly pending = signal<PendingCheckIn[]>([]);
  readonly syncing = signal(false);

  constructor() {
    const destroyRef = inject(DestroyRef);
    if (typeof window !== 'undefined') {
      merge(fromEvent(window, 'online'), fromEvent(window, 'offline'))
        .pipe(takeUntilDestroyed(destroyRef))
        .subscribe(() => {
          const online = navigator.onLine;
          this.online.set(online);
          // Voltou a rede com fila pendente: sincroniza sozinho.
          if (online && this.showId && this.pending().length > 0 && !this.syncing()) {
            this.syncPending();
          }
        });
    }
    this.catalog.listShows('', 0, 50).subscribe({
      next: (page) => {
        this.shows.set(page.items);
        const published = page.items.find((s) => s.published);
        if (published) {
          this.showId = published.id;
          this.refreshPreloadMeta();
        }
      },
      error: () => this.error.set(this.i18n.t('errors.loadShows')),
    });
  }

  onShowChange(id: string): void {
    this.showId = id;
    this.reset();
    this.refreshPreloadMeta();
  }

  refreshPreloadMeta(): void {
    if (!this.showId) {
      this.preload.set(null);
      this.pending.set([]);
      return;
    }
    this.preload.set(this.cache.getPreload(this.showId));
    this.pending.set(this.cache.getPending(this.showId));
  }

  downloadPreload(): void {
    if (!this.showId || !this.online() || this.preloading()) return;
    this.preloading.set(true);
    this.tickets.preload(this.showId).subscribe({
      next: (tickets) => {
        this.preload.set(this.cache.savePreload(this.showId, tickets));
        this.preloading.set(false);
        this.toast.show(this.i18n.t('gate.preloaded', { count: tickets.length }));
      },
      error: (err) => {
        this.preloading.set(false);
        this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
      },
    });
  }

  /** Envia a fila de check-ins offline; êxitos e "já usado" saem da fila. */
  syncPending(): void {
    const queue = this.pending();
    if (!this.showId || !this.online() || queue.length === 0 || this.syncing()) return;
    this.syncing.set(true);
    const showId = this.showId;
    forkJoin(
      queue.map((item) =>
        this.tickets.validate(showId, item.ticketId, item.code, item.signature).pipe(
          map(() => ({ ticketId: item.ticketId, done: true })),
          catchError((err) =>
            of({ ticketId: item.ticketId, done: isAlreadyUsed(err) }),
          ),
        ),
      ),
    ).subscribe({
      next: (results) => {
        const done = results.filter((r) => r.done).map((r) => r.ticketId);
        this.pending.set(this.cache.dropPending(showId, done));
        this.syncing.set(false);
        this.toast.show(this.i18n.t('gate.synced', { ok: done.length, total: queue.length }));
        // Atualiza status vindos do servidor (viraram USED lá).
        if (done.length > 0) this.downloadPreload();
      },
      error: () => this.syncing.set(false),
    });
  }

  validate(): void {
    this.error.set(null);
    this.result.set(null);
    this.offlineResult.set(false);
    const parts = this.qr.trim().split(':');
    if (!this.showId || parts.length !== 3 || parts.some((p) => !p)) {
      this.state.set('denied');
      return;
    }
    const [ticketId, code] = parts as [string, string, string];
    // Offline: valida contra a pré-carga (ticketId:código + ainda não usado).
    if (!this.online()) {
      this.validateOffline(ticketId, code);
      return;
    }
    this.state.set('checking');
    this.tickets.validate(this.showId, parts[0], parts[1], parts[2]).subscribe({
      next: (res) => {
        this.cache.remember(this.showId, res);
        this.result.set(res);
        this.state.set('granted');
      },
      error: (err) => {
        // A rede pode ter caído entre o clique e a resposta: tenta o cache.
        if (isOfflineError(err)) {
          this.online.set(false);
          this.validateOffline(ticketId, code);
          return;
        }
        // Releitura de algo já validado neste aparelho: mostra o cache.
        const cached = this.cache.lookup(this.showId, ticketId);
        if (cached && cached.code === code) {
          this.result.set(cached);
          this.offlineResult.set(true);
          this.state.set('granted');
          return;
        }
        this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
        this.state.set('denied');
      },
    });
  }

  /**
   * Validação offline contra a pré-carga: libera se o par existe, está
   * ISSUED e não foi usado (nem localmente); enfileira o check-in para
   * sincronizar quando a rede voltar.
   */
  private validateOffline(ticketId: string, code: string): void {
    const entry = this.preload()?.entries[ticketId];
    if (!entry || entry.code !== code) {
      const cached = this.cache.lookup(this.showId, ticketId);
      if (cached && cached.code === code) {
        this.result.set(cached);
        this.offlineResult.set(true);
        this.state.set('granted');
      } else {
        this.error.set(this.i18n.t('gate.noConnection'));
        this.state.set('denied');
      }
      return;
    }
    if (entry.status !== 'ISSUED' || entry.usedLocally) {
      this.error.set(this.i18n.t('gate.alreadyUsed'));
      this.state.set('denied');
      return;
    }
    this.cache.markPreloadUsed(this.showId, ticketId);
    this.preload.set(this.cache.getPreload(this.showId));
    this.pending.set(
      this.cache.enqueuePending(this.showId, {
        ticketId,
        code,
        signature: entry.signature,
      }),
    );
    const show = this.shows().find((s) => s.id === this.showId);
    this.result.set({
      showId: this.showId,
      ticketId,
      code,
      orderId: '',
      spotId: '',
      location: entry.location,
      showDate: show?.date ?? '',
      checkedInAt: new Date().toISOString(),
    });
    this.offlineResult.set(true);
    this.state.set('granted');
  }

  reset(): void {
    this.qr = '';
    this.state.set('idle');
    this.result.set(null);
    this.error.set(null);
    this.offlineResult.set(false);
  }
}

function isOfflineError(err: unknown): boolean {
  if (typeof navigator !== 'undefined' && !navigator.onLine) return true;
  const status = (err as { status?: number })?.status;
  return status === 0 || status === undefined;
}

/** Erro de validate quando o ingresso já foi usado ( sai da fila de sync). */
function isAlreadyUsed(err: unknown): boolean {
  const msg = (
    (err as { error?: { errors?: { message: string }[] } })?.error?.errors?.[0]
      ?.message ?? ''
  ).toLowerCase();
  return msg.includes('already used') || msg.includes('já utili');
}
