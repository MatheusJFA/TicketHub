import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfigService } from './config.service';
import { OrderResponse, PayOrderResponse } from './models';

@Injectable({ providedIn: 'root' })
export class OrdersService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  createOrder(customerId: string, spotIds: string[], idempotencyKey: string) {
    return this.http.post<OrderResponse>(
      `${this.config.url()}/orders`,
      { customerId, spotIds },
      { headers: { 'Idempotency-Key': idempotencyKey } },
    );
  }

  payOrder(orderId: string) {
    return this.http.post<PayOrderResponse>(
      `${this.config.url()}/orders/${orderId}/pay`,
      {},
    );
  }

  getOrder(orderId: string) {
    return this.http.get<OrderResponse>(`${this.config.url()}/orders/${orderId}`);
  }

  cancelOrder(orderId: string) {
    return this.http.post<OrderResponse>(`${this.config.url()}/orders/${orderId}/cancel`, {});
  }
}
