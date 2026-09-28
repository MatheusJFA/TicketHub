import { describe, expect, it } from 'vitest';
import { ConfirmService } from './confirm.service';

describe('ConfirmService', () => {
  it('resolve true ao confirmar', async () => {
    const service = new ConfirmService();
    const pending = service.ask('Excluir?');
    expect(service.pending()?.message).toBe('Excluir?');
    service.confirm();
    await expect(pending).resolves.toBe(true);
    expect(service.pending()).toBeNull();
  });

  it('resolve false ao cancelar', async () => {
    const service = new ConfirmService();
    const pending = service.ask('Excluir?');
    service.cancel();
    await expect(pending).resolves.toBe(false);
  });

  it('um novo ask cancela o anterior', async () => {
    const service = new ConfirmService();
    const first = service.ask('Um?');
    const second = service.ask('Dois?');
    await expect(first).resolves.toBe(false);
    service.confirm();
    await expect(second).resolves.toBe(true);
  });
});
