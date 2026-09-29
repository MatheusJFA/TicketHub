import { Injectable, signal } from '@angular/core';

export interface ConfirmRequest {
  message: string;
}

/**
 * Substitui `window.confirm` (bloqueante, sem i18n/a11y) por um diálogo
 * acessível. `ask()` resolve `true` ao confirmar, `false` ao cancelar/Esc.
 */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly pending = signal<ConfirmRequest | null>(null);
  private resolver: ((value: boolean) => void) | null = null;

  ask(message: string): Promise<boolean> {
    this.cancel();
    this.pending.set({ message });
    return new Promise<boolean>((resolve) => {
      this.resolver = resolve;
    });
  }

  confirm(): void {
    this.resolver?.(true);
    this.cleanup();
  }

  cancel(): void {
    this.resolver?.(false);
    this.cleanup();
  }

  private cleanup(): void {
    this.resolver = null;
    this.pending.set(null);
  }
}
