import { describe, expect, it } from 'vitest';
import { I18nService } from './i18n.service';

describe('I18nService', () => {
  it('traduz com fallback pt e interpola params', () => {
    const service = new I18nService();
    service.init();
    expect(service.t('checkout.retry')).toBeTruthy();
    expect(service.t('checkout.paidSeats', { seats: 'A1, A2' })).toContain('A1, A2');
  });

  it('troca idioma e dateLocale acompanha', () => {
    const service = new I18nService();
    service.setLang('en');
    expect(service.lang()).toBe('en');
    expect(service.dateLocale()).toBe('en-US');
    service.setLang('pt');
    expect(service.dateLocale()).toBe('pt-BR');
  });
});
