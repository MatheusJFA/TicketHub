import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfigService } from '../core/config.service';
import { IdResponse } from './admin.service';

export interface CreateCouponInput {
  code: string;
  showId?: string | null;
  sectionId?: string | null;
  kind: 'PERCENT' | 'FIXED';
  percent?: number | null;
  fixedValue?: number | null;
  fixedCurrency?: string | null;
  validFrom?: string | null;
  validUntil?: string | null;
  maxUses?: number | null;
}

@Injectable({ providedIn: 'root' })
export class CouponsService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  create(input: CreateCouponInput) {
    return this.http.post<IdResponse>(`${this.config.url()}/coupons`, input);
  }
}
