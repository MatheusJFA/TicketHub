import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ConfigService } from './config.service';

export interface CustomerTicket {
  ticketId: string;
  code: string;
  signature: string;
  status: string;
  orderId: string;
  spotId: string;
  location: string;
  showId: string;
  showName: string;
  showDate: string;
}

export interface GateTicket {
  ticketId: string;
  code: string;
  signature: string;
  status: string;
  spotId: string;
  location: string;
}

export interface ValidateTicketResponse {
  showId: string;
  ticketId: string;
  code: string;
  orderId: string;
  spotId: string;
  location: string;
  showDate: string;
  checkedInAt: string;
}

@Injectable({ providedIn: 'root' })
export class TicketsService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  /** QR payload scanned at the door: {@code ticketId:code:signature}. */
  qrPayload(ticket: Pick<CustomerTicket, 'ticketId' | 'code' | 'signature'>): string {
    return `${ticket.ticketId}:${ticket.code}:${ticket.signature}`;
  }

  listMine(customerId: string) {
    const params = new HttpParams({ fromObject: { customerId } });
    return this.http.get<CustomerTicket[]>(`${this.config.url()}/tickets`, { params });
  }

  /** Pré-carga da portaria: todos os ingressos do show para validar offline. */
  preload(showId: string) {
    return this.http.get<GateTicket[]>(`${this.config.url()}/shows/${showId}/tickets`);
  }

  validate(showId: string, ticketId: string, code: string, signature: string) {
    return this.http.post<ValidateTicketResponse>(
      `${this.config.url()}/shows/${showId}/tickets/validate`,
      { ticketId, code, signature },
    );
  }
}
