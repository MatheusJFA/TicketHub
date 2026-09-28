import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ListErrorComponent } from '../../core/list-error.component';
import { IconComponent } from '../../core/icon.component';
import { ToastService } from '../../core/toast.service';
import { TicketsService, CustomerTicket } from '../../core/tickets.service';

@Component({
  selector: 'app-my-tickets',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, ListErrorComponent, IconComponent],
  templateUrl: './my-tickets.component.html',
})
export class MyTicketsComponent {
  private readonly auth = inject(AuthService);
  private readonly tickets = inject(TicketsService);
  private readonly toast = inject(ToastService);
  readonly i18n = inject(I18nService);

  readonly items = signal<CustomerTicket[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly copiedId = signal<string | null>(null);
  /** QR renderizado (dataURL) por ticketId — lazy via dynamic import, fora do bundle inicial. */
  readonly qrImages = signal<Record<string, string>>({});

  constructor() {
    this.load();
  }

  load(): void {
    const customerId = this.auth.customerId();
    if (!customerId) {
      this.error.set(this.i18n.t('errors.noCustomer'));
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.tickets.listMine(customerId).subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
        void this.renderQrs(items);
      },
      error: () => {
        this.error.set(this.i18n.t('errors.generic'));
        this.loading.set(false);
      },
    });
  }

  qr(ticket: CustomerTicket): string {
    return this.tickets.qrPayload(ticket);
  }

  qrImage(ticket: CustomerTicket): string | null {
    return this.qrImages()[ticket.ticketId] ?? null;
  }

  /** Exibe só o ticketId; signature completa fica no <details> + copiar. */
  shortQr(ticket: CustomerTicket): string {
    return `${ticket.ticketId.slice(0, 8)}…`;
  }

  copy(ticket: CustomerTicket): void {
    navigator.clipboard
      .writeText(this.qr(ticket))
      .then(() => {
        this.copiedId.set(ticket.ticketId);
        this.toast.show(this.i18n.t('checkout.copied'));
        setTimeout(() => this.copiedId.set(null), 2000);
      })
      .catch(() => undefined);
  }

  private async renderQrs(items: CustomerTicket[]): Promise<void> {
    try {
      const { default: QRCode } = await import('qrcode');
      const entries = await Promise.all(
        items.slice(0, 50).map(async (t) => {
          try {
            const url = await QRCode.toDataURL(this.qr(t), { margin: 1, width: 180 });
            return [t.ticketId, url] as const;
          } catch {
            return null;
          }
        }),
      );
      const map: Record<string, string> = {};
      for (const e of entries) if (e) map[e[0]] = e[1];
      this.qrImages.set(map);
    } catch {
      // sem QR: fallback é o código manual
    }
  }
}
