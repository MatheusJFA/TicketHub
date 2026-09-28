import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfigService } from './config.service';

export interface CustomerProfile {
  id: string;
  name: string;
  cpf: string;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  get(id: string) {
    return this.http.get<CustomerProfile>(`${this.config.url()}/customers/${id}`);
  }

  rename(id: string, name: string) {
    return this.http.patch(`${this.config.url()}/customers/${id}/name`, { name });
  }

  remove(id: string) {
    return this.http.delete<void>(`${this.config.url()}/customers/${id}`);
  }
}
