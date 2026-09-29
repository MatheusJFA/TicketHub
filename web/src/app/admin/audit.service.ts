import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ConfigService } from '../core/config.service';
import { Pagination } from '../core/models';

export interface AuditEntry {
  id: string;
  occurredAt: string;
  correlationId: string;
  actor: string;
  action: string;
  input: string | null;
  outcome: string;
  error: string | null;
  durationMs: number;
}

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  list(filters: { action?: string; actor?: string; outcome?: string } = {}, page = 0, perPage = 20) {
    let params = new HttpParams({ fromObject: { page, perPage } });
    for (const [k, v] of Object.entries(filters)) {
      if (v?.trim()) params = params.set(k, v.trim());
    }
    return this.http.get<Pagination<AuditEntry>>(`${this.config.url()}/audit-logs`, { params });
  }
}
