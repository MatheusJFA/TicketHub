import {
  ApplicationConfig,
  LOCALE_ID,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection,
} from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import localeEs from '@angular/common/locales/es';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import {
  provideRouter,
  withComponentInputBinding,
  withInMemoryScrolling,
  withPreloading,
  withViewTransitions,
  PreloadAllModules,
} from '@angular/router';
import { routes } from './app.routes';
import { authInterceptor } from './auth/auth.interceptor';
import { ConfigService } from './core/config.service';
import { LANG_STORAGE_KEY } from './shared/i18n/locales';
import { I18nService } from './shared/i18n/i18n.service';

registerLocaleData(localePt);
registerLocaleData(localeEs);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),
    provideBrowserGlobalErrorListeners(),
    provideRouter(
      routes,
      withComponentInputBinding(),
      withPreloading(PreloadAllModules),
      withInMemoryScrolling({ scrollPositionRestoration: 'top' }),
      withViewTransitions(),
    ),
    provideAppInitializer(() => inject(ConfigService).load()),
    provideAppInitializer(() => inject(I18nService).init()),
    { provide: LOCALE_ID, useFactory: detectBrowserLocale },
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
  ],
};

/** LOCALE_ID inicial a partir do navegador; I18nService assume e sincroniza depois. */
function detectBrowserLocale(): string {
  try {
    const stored = localStorage.getItem(LANG_STORAGE_KEY);
    if (stored === 'en') return 'en-US';
    if (stored === 'es') return 'es';
    if (stored === 'pt') return 'pt-BR';
    const nav = (navigator?.language ?? 'pt-BR').toLowerCase();
    if (nav.startsWith('es')) return 'es';
    if (nav.startsWith('en')) return 'en-US';
  } catch {
    /* SSR ou storage indisponível */
  }
  return 'pt-BR';
}
