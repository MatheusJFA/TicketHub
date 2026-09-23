import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { OrdersService } from '../../core/orders.service';
import { OrderResponse } from '../../core/models';

@Component({
  selector: 'app-order-status',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './order-status.component.html',
})
export class OrderStatusComponent {
  private readonly orders = inject(OrdersService);
  private readonly route = inject(ActivatedRoute);

  orderId = this.route.snapshot.paramMap.get('id') ?? '';
  readonly order = signal<OrderResponse | null>(null);
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);
  readonly cancelling = signal(false);
  readonly cancelError = signal<string | null>(null);

  constructor() {
    if (this.orderId) {
      this.lookup();
    }
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
        this.error.set('Pedido não encontrado.');
        this.loading.set(false);
      },
    });
  }

  cancel(): void {
    const order = this.order();
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
        this.cancelError.set(
          err?.error?.errors?.[0]?.message ?? 'Não foi possível cancelar.',
        );
      },
    });
  }
}
