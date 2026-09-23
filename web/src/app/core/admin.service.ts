import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfigService } from './config.service';

export interface IdResponse {
  id: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  private url(): string {
    return this.config.url();
  }

  createShow(input: {
    partnerId: string;
    name: string;
    description: string;
    date: string;
    address: Record<string, string | null>;
  }) {
    return this.http.post<IdResponse>(`${this.url()}/shows`, input);
  }

  publishShow(id: string) {
    return this.http.post<IdResponse>(`${this.url()}/shows/${id}/publish`, {});
  }

  addSection(
    showId: string,
    input: { name: string; description: string; totalSpots: number; price: { value: number; currency: string } },
  ) {
    return this.http.post<IdResponse>(`${this.url()}/shows/${showId}/sections`, input);
  }

  publishSection(id: string) {
    return this.http.post<IdResponse>(`${this.url()}/sections/${id}/publish`, {});
  }

  createSpot(sectionId: string, location: string) {
    return this.http.post<IdResponse>(`${this.url()}/spots`, { sectionId, location });
  }

  publishSpot(id: string) {
    return this.http.post<IdResponse>(`${this.url()}/spots/${id}/publish`, {});
  }
}
