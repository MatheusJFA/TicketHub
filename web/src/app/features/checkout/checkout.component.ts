import { ChangeDetectionStrategy, Component, inject, OnDestroy, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { interval, Subscription, switchMap, take, takeWhile } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { OrdersService } from '../../core/orders.service';
import { shareLink } from '../../core/share';
import { ToastService } from '../../core/toast.service';
import { parseApiError } from '../../core/api-error';
import { IconComponent } from '../../core/icon.component';
import { OrderResponse, PayOrderResponse } from '../../core/models';

type Phase = 'review' | 'paying' | 'waiting' | 'done' | 'error';

@Component({
  selector: 'app-checkout',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, IconComponent],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent implements OnDestroy {
  private readonly auth = inject(AuthService);
  readonly cart = inject(CartService);
  readonly i18n = inject(I18nService);
  private readonly orders = inject(OrdersService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly phase = signal<Phase>('review');
  readonly order = signal<OrderResponse | null>(null);
  readonly payment = signal<PayOrderResponse | null>(null);
  readonly error = signal<string | null>(null);
  readonly copied = signal(false);
  private polling: Subscription | null = null;
  private idempotencyKey: string | null = null;
  /** ~2 min de polling (40 x 3s) antes de desistir e pedir retry manual. */
  private static readonly MAX_POLLS = 40;

  confirm(): void {
    // Retry reaproveita a order existente em vez de criar novo hold duplicado.
    const existing = this.order();
    if (existing && (this.phase() === 'error' || this.phase() === 'waiting')) {
      this.phase.set('paying');
      this.error.set(null);
      this.pay(existing.orderId);
      return;
    }
    const customerId = this.auth.customerId();
    if (!customerId) {
      this.error.set(this.i18n.t('errors.noCustomer'));
      return;
    }
    const spotIds = this.cart.ids();
    if (spotIds.length === 0) {
      this.router.navigate(['/']);
      return;
    }
    this.phase.set('paying');
    this.error.set(null);
    this.idempotencyKey ??= newIdempotencyKey();
    this.orders.createOrder(customerId, spotIds, this.idempotencyKey).subscribe({
      next: (order) => {
        this.order.set(order);
        this.pay(order.orderId);
      },
      error: (err) => this.fail(err),
    });
  }

  private pay(orderId: string): void {
    this.orders.payOrder(orderId).subscribe({
      next: (payment) => {
        this.payment.set(payment);
        this.phase.set('waiting');
        this.poll(orderId);
      },
      error: (err) => this.fail(err),
    });
  }

  private poll(orderId: string): void {
    this.polling?.unsubscribe();
    this.polling = interval(3000)
      .pipe(
        switchMap(() => this.orders.getOrder(orderId)),
        takeWhile((order) => order.status === 'PENDING', true),
        take(CheckoutComponent.MAX_POLLS),
      )
      .subscribe({
        next: (order) => {
          this.order.set(order);
          if (order.status !== 'PENDING') {
            this.phase.set('done');
            this.idempotencyKey = null;
            this.cart.clear();
          }
        },
        error: (err) => this.fail(err),
        complete: () => {
          // Atingiu MAX_POLLS ainda em PENDING: para de poluir o backend.
          if (this.phase() === 'waiting' && this.order()?.status === 'PENDING') {
            this.fail({ timeout: true });
          }
        },
      });
  }

  shareOrder(): void {
    const order = this.order();
    if (!order) return;
    const url = `${location.origin}/orders/${order.orderId}`;
    void shareLink(url, this.i18n.t('orders.title'), this.i18n.t('checkout.copied'), this.toast);
  }

  copyCode(): void {
    const code = this.payment()?.paymentCode;
    if (code) {
      navigator.clipboard
        .writeText(code)
        .then(() => {
          this.copied.set(true);
          this.toast.show(this.i18n.t('checkout.copied'));
          setTimeout(() => this.copied.set(false), 2000);
        })
        .catch(() => undefined);
    }
  }

  ngOnDestroy(): void {
    this.polling?.unsubscribe();
  }

  private fail(err: unknown): void {
    this.polling?.unsubscribe();
    this.polling = null;
    if ((err as { timeout?: boolean })?.timeout) {
      this.error.set(this.i18n.t('checkout.timeout'));
      this.phase.set('waiting');
      return;
    }
    const message = parseApiError(err, this.i18n.t('errors.generic'));
    this.error.set(message);
    this.phase.set('error');
  }
}

function newIdempotencyKey(): string {
  try {
    if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
      return crypto.randomUUID();
    }
  } catch {
    // fallback abaixo
  }
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`;
}
