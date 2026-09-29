import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfigService } from '../core/config.service';

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

  unpublishShow(id: string) {
    return this.http.post<IdResponse>(`${this.url()}/shows/${id}/unpublish`, {});
  }

  deleteShow(id: string) {
    return this.http.delete<void>(`${this.url()}/shows/${id}`);
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

  publishAllSection(id: string) {
    return this.http.post<IdResponse>(`${this.url()}/sections/${id}/publish-all`, {});
  }

  unpublishAllSection(id: string) {
    return this.http.post<IdResponse>(`${this.url()}/sections/${id}/unpublish-all`, {});
  }

  renameSection(id: string, name: string) {
    return this.http.patch<IdResponse>(`${this.url()}/sections/${id}/name`, { name });
  }

  changeSectionPrice(id: string, value: number, currency = 'BRL') {
    return this.http.patch<IdResponse>(`${this.url()}/sections/${id}/price`, {
      price: { value, currency },
    });
  }

  deleteSection(id: string) {
    return this.http.delete<void>(`${this.url()}/sections/${id}`);
  }

  createSpot(sectionId: string, location: string) {
    return this.http.post<IdResponse>(`${this.url()}/spots`, { sectionId, location });
  }

  renameSpot(id: string, location: string) {
    return this.http.patch<IdResponse>(`${this.url()}/spots/${id}/location`, { location });
  }

  deleteSpot(id: string) {
    return this.http.delete<void>(`${this.url()}/spots/${id}`);
  }

  publishSpot(id: string) {
    return this.http.post<IdResponse>(`${this.url()}/spots/${id}/publish`, {});
  }
}
