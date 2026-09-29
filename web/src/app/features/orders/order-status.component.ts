import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { I18nService } from '../../shared/i18n/i18n.service';
import { ListErrorComponent } from '../../shared/ui/list-error.component';
import { IconComponent } from '../../shared/ui/icon.component';
import { OrdersService } from '../../orders/orders.service';
import { shareLink } from '../../shared/share';
import { ToastService } from '../../shared/ui/toast.service';
import { parseApiError } from '../../core/api-error';
import { OrderResponse } from '../../core/models';

@Component({
  selector: 'app-order-status',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe, ListErrorComponent, IconComponent],
  templateUrl: './order-status.component.html',
})
export class OrderStatusComponent {
  private readonly orders = inject(OrdersService);
  private readonly toast = inject(ToastService);
  readonly i18n = inject(I18nService);

  /** Rota /orders/:id via withComponentInputBinding; /orders usa '' + lookup manual. */
  readonly id = input<string>('');

  orderId = '';
  readonly order = signal<OrderResponse | null>(null);
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);
  readonly cancelling = signal(false);
  readonly cancelError = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.id();
      if (id) {
        this.orderId = id;
        this.lookup();
      }
    });
  }

  lookup(): void {
    if (!this.orderId) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.orders.getOrder(this.orderId).subscribe({
      next: (order) => {
        this.order.set(order);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(this.i18n.t('errors.orderNotFound'));
        this.loading.set(false);
      },
    });
  }

  share(): void {
    const order = this.order();
    if (!order) return;
    const url = `${location.origin}/orders/${order.orderId}`;
    void shareLink(url, this.i18n.t('orders.title'), this.i18n.t('checkout.copied'), this.toast);
  }

  cancel(): void {    const order = this.order();
    if (!order || order.status !== 'PENDING') {
      return;
    }
    this.cancelling.set(true);
    this.cancelError.set(null);
    this.orders.cancelOrder(order.orderId).subscribe({
      next: (cancelled) => {
        this.order.set(cancelled);
        this.cancelling.set(false);
      },
      error: (err) => {
        this.cancelling.set(false);
        this.cancelError.set(parseApiError(err, this.i18n.t('errors.cancelFailed')));
      },
    });
  }
}
