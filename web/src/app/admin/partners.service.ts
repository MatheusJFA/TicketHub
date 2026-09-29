import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ConfigService } from '../core/config.service';
import { Address, Pagination } from '../core/models';
import { IdResponse } from './admin.service';

export interface PartnerProfile {
  id: string;
  name: string;
  cnpj: string;
  address: Address;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

export interface PartnerStatus {
  id: string;
  status: string;
}

export interface PartnerSignupInput {
  name: string;
  cnpj: string;
  address: Address;
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class PartnersService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  /** Public registration request (starts PENDING approval). */
  signup(input: PartnerSignupInput) {
    return this.http.post<IdResponse>(`${this.config.url()}/partners`, input);
  }

  get(id: string) {
    return this.http.get<PartnerProfile>(`${this.config.url()}/partners/${id}`);
  }

  update(id: string, input: { name: string; address: Address }) {
    return this.http.put<IdResponse>(`${this.config.url()}/partners/${id}`, input);
  }

  changeWebhook(id: string, webhookUrl: string, webhookSecret: string) {
    return this.http.put<IdResponse>(`${this.config.url()}/partners/${id}/webhook`, {
      webhookUrl,
      webhookSecret,
    });
  }

  list(page = 0, perPage = 20) {
    const params = new HttpParams({ fromObject: { page, perPage } });
    return this.http.get<Pagination<PartnerProfile>>(`${this.config.url()}/partners`, { params });
  }

  approve(id: string) {
    return this.http.post<PartnerStatus>(`${this.config.url()}/partners/${id}/approve`, {});
  }

  reject(id: string) {
    return this.http.post<PartnerStatus>(`${this.config.url()}/partners/${id}/reject`, {});
  }

  remove(id: string) {
    return this.http.delete<void>(`${this.config.url()}/partners/${id}`);
  }
}
