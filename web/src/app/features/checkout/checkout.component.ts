import { Component, OnDestroy, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { interval, Subscription, switchMap, takeWhile } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { OrdersService } from '../../core/orders.service';
import { OrderResponse, PayOrderResponse } from '../../core/models';

type Phase = 'review' | 'paying' | 'waiting' | 'done' | 'error';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent implements OnDestroy {
  private readonly auth = inject(AuthService);
  readonly cart = inject(CartService);
  private readonly orders = inject(OrdersService);
  private readonly router = inject(Router);

  readonly phase = signal<Phase>('review');
  readonly order = signal<OrderResponse | null>(null);
  readonly payment = signal<PayOrderResponse | null>(null);
  readonly error = signal<string | null>(null);
  private polling: Subscription | null = null;

  confirm(): void {
    const customerId = this.auth.customerId();
    if (!customerId) {
      this.error.set('Sua sessão não tem cliente vinculado. Entre com um usuário cliente.');
      return;
    }
    const spotIds = this.cart.ids();
    if (spotIds.length === 0) {
      this.router.navigate(['/seats']);
      return;
    }
    this.phase.set('paying');
    this.error.set(null);
    const idempotencyKey = crypto.randomUUID();
    this.orders.createOrder(customerId, spotIds, idempotencyKey).subscribe({
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
      )
      .subscribe({
        next: (order) => {
          this.order.set(order);
          if (order.status !== 'PENDING') {
            this.phase.set('done');
            this.cart.clear();
          }
        },
        error: (err) => this.fail(err),
      });
  }

  copyCode(): void {
    const code = this.payment()?.paymentCode;
    if (code) {
      navigator.clipboard.writeText(code).catch(() => undefined);
    }
  }

  ngOnDestroy(): void {
    this.polling?.unsubscribe();
  }

  private fail(err: unknown): void {
    const message =
      (err as { error?: { errors?: { message: string }[] } })?.error?.errors?.[0]?.message ??
      'Algo deu errado. Tente novamente.';
    this.error.set(message);
    this.phase.set('error');
  }
}
