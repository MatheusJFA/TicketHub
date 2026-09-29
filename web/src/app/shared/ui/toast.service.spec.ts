import { afterEach, describe, expect, it, vi } from 'vitest';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('adiciona e auto-dispensa o toast', () => {
    vi.useFakeTimers();
    const service = new ToastService();
    service.show('copiado');
    expect(service.toasts()).toHaveLength(1);

    vi.advanceTimersByTime(3000);
    expect(service.toasts()).toHaveLength(0);
  });

  it('ignora mensagem vazia', () => {
    const service = new ToastService();
    service.show('');
    expect(service.toasts()).toHaveLength(0);
  });
});
