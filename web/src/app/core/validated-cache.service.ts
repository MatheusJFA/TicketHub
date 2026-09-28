import { Injectable } from '@angular/core';
import { GateTicket, ValidateTicketResponse } from './tickets.service';

const PREFIX = 'tickethub.validated.';
const PRELOAD_PREFIX = 'tickethub.preload.';
const PENDING_PREFIX = 'tickethub.pending.';
/** Limite por show para não estourar a cota do localStorage. */
const MAX_PER_SHOW = 1000;

export interface PreloadEntry {
  code: string;
  signature: string;
  status: string;
  location: string;
  /** Marcado localmente após check-in offline (ainda não sincronizado). */
  usedLocally: boolean;
}

export interface PreloadStore {
  savedAt: string;
  entries: Record<string, PreloadEntry>;
}

export interface PendingCheckIn {
  ticketId: string;
  code: string;
  signature: string;
}

/**
 * Cache local da portaria: guarda ingressos já validados (com sucesso) para
 * que releituras continuem funcionando sem conexão — evita liberar duas
 * vezes o mesmo ingresso quando a rede oscila. Não valida assinatura
 * offline: só reconhece o que este dispositivo já viu passar.
 */
@Injectable({ providedIn: 'root' })
export class ValidatedCacheService {
  /** Fallback em memória (SSR, testes, private mode). */
  private readonly memory = new Map<string, Record<string, ValidateTicketResponse>>();
  private readonly jsonMemory = new Map<string, string>();

  remember(showId: string, ticket: ValidateTicketResponse): void {
    const all = this.readAll(showId);
    all[ticket.ticketId] = ticket;
    const ids = Object.keys(all);
    if (ids.length > MAX_PER_SHOW) {
      for (const drop of ids.slice(0, ids.length - MAX_PER_SHOW)) delete all[drop];
    }
    this.writeAll(showId, all);
  }

  lookup(showId: string, ticketId: string): ValidateTicketResponse | null {
    return this.readAll(showId)[ticketId] ?? null;
  }

  clear(showId: string): void {
    try {
      localStorage?.removeItem(PREFIX + showId);
    } catch {
      /* ignora */
    }
    this.memory.delete(showId);
  }

  // ---- preload da portaria (GET /shows/{id}/tickets) ----

  savePreload(showId: string, tickets: GateTicket[]): PreloadStore {
    const entries: Record<string, PreloadEntry> = {};
    for (const t of tickets.slice(0, MAX_PER_SHOW)) {
      entries[t.ticketId] = {
        code: t.code,
        signature: t.signature,
        status: t.status,
        location: t.location,
        usedLocally: false,
      };
    }
    const store: PreloadStore = { savedAt: new Date().toISOString(), entries };
    this.writeJson(PRELOAD_PREFIX + showId, store);
    return store;
  }

  getPreload(showId: string): PreloadStore | null {
    const raw = this.readJson(PRELOAD_PREFIX + showId);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as PreloadStore;
    } catch {
      return null;
    }
  }

  /** Marca como usado localmente após check-in offline. */
  markPreloadUsed(showId: string, ticketId: string): void {
    const store = this.getPreload(showId);
    if (!store?.entries[ticketId]) return;
    store.entries[ticketId].usedLocally = true;
    this.writeJson(PRELOAD_PREFIX + showId, store);
  }

  // ---- fila de check-ins offline para sincronizar ----

  enqueuePending(showId: string, item: PendingCheckIn): PendingCheckIn[] {
    const queue = this.getPending(showId).filter((p) => p.ticketId !== item.ticketId);
    queue.push(item);
    this.writeJson(PENDING_PREFIX + showId, queue);
    return queue;
  }

  getPending(showId: string): PendingCheckIn[] {
    const raw = this.readJson(PENDING_PREFIX + showId);
    if (!raw) return [];
    try {
      return JSON.parse(raw) as PendingCheckIn[];
    } catch {
      return [];
    }
  }

  /** Remove da fila os enviados com sucesso (mantém o resto). */
  dropPending(showId: string, ticketIds: string[]): PendingCheckIn[] {
    const remaining = this.getPending(showId).filter((p) => !ticketIds.includes(p.ticketId));
    this.writeJson(PENDING_PREFIX + showId, remaining);
    return remaining;
  }

  private readAll(showId: string): Record<string, ValidateTicketResponse> {
    try {
      const raw = localStorage?.getItem(PREFIX + showId);
      if (raw) return JSON.parse(raw) as Record<string, ValidateTicketResponse>;
    } catch {
      /* JSON corrompido ou storage indisponível: usa memória */
    }
    return this.memory.get(showId) ?? {};
  }

  private writeAll(showId: string, all: Record<string, ValidateTicketResponse>): void {
    this.memory.set(showId, all);
    try {
      localStorage?.setItem(PREFIX + showId, JSON.stringify(all));
    } catch {
      /* quota cheia ou indisponível: memória segue valendo na sessão */
    }
  }

  private writeJson(key: string, value: unknown): void {
    const raw = JSON.stringify(value);
    this.jsonMemory.set(key, raw);
    try {
      localStorage?.setItem(key, raw);
    } catch {
      /* quota cheia ou indisponível: memória segue valendo na sessão */
    }
  }

  private readJson(key: string): string | null {
    try {
      const raw = localStorage?.getItem(key);
      if (raw) return raw;
    } catch {
      /* indisponível: cai na memória */
    }
    return this.jsonMemory.get(key) ?? null;
  }
}
