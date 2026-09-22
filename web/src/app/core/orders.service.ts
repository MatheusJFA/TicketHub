import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { OrderResponse, PayOrderResponse } from './models';

@Injectable({ providedIn: 'root' })
export class OrdersService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  createOrder(customerId: string, spotIds: string[], idempotencyKey: string) {
    return this.http.post<OrderResponse>(
      `${this.base}/orders`,
      { customerId, spotIds },
      { headers: { 'Idempotency-Key': idempotencyKey } },
    );
  }

  payOrder(orderId: string) {
    return this.http.post<PayOrderResponse>(
      `${this.base}/orders/${orderId}/pay`,
      {},
    );
  }

  getOrder(orderId: string) {
    return this.http.get<OrderResponse>(`${this.base}/orders/${orderId}`);
  }
}
