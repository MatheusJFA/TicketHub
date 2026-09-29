import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
}

let nextId = 1;

/** Toast global reativo: show() exibe e auto-dispensa em 3s. */
@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);

  show(message: string, timeoutMs = 3000): void {
    if (!message) return;
    const id = nextId++;
    this.toasts.update((list) => [...list, { id, message }]);
    setTimeout(() => this.dismiss(id), timeoutMs);
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }
}
