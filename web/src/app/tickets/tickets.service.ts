import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ConfigService } from '../core/config.service';

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

export interface BuyerTicket {
  ticketId: string;
  status: string;
  spotId: string;
  location: string;
  buyerName: string;
  buyerCpf: string;
  buyerEmail: string;
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

  /** Lista de compradores do show (nome, CPF, e-mail por ingresso). */
  buyers(showId: string) {
    return this.http.get<BuyerTicket[]>(`${this.config.url()}/shows/${showId}/buyers`);
  }

  validate(showId: string, ticketId: string, code: string, signature: string) {
    return this.http.post<ValidateTicketResponse>(
      `${this.config.url()}/shows/${showId}/tickets/validate`,
      { ticketId, code, signature },
    );
  }
}
