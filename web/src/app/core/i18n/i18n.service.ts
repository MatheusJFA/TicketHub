import { Injectable, computed, signal } from '@angular/core';
import { AppLang, DICTS, LANG_STORAGE_KEY, LANGS, TranslationKey } from './locales';

export type { AppLang };

/**
 * Runtime i18n: signal-based language state with PT/EN/ES dictionaries.
 * Templates call `i18n.t('key')` (or with `{params}`) so the view
 * re-renders automatically when the language changes — no rebuild,
 * no extra dependency.
 */
@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly state = signal<AppLang>('pt');

  readonly lang = this.state.asReadonly();
  readonly langs = LANGS;

  /** Locale string for Angular date/number pipes (e.g. 'pt-BR'). */
  readonly dateLocale = computed(
    () => LANGS.find((l) => l.id === this.state())?.dateLocale ?? 'pt-BR',
  );

  /** Reads persisted language (or browser language) before first paint. */
  init(): void {
    this.state.set(detectInitialLang());
    this.applyHtmlLang(this.state());
  }

  setLang(lang: AppLang): void {
    this.state.set(lang);
    try {
      localStorage.setItem(LANG_STORAGE_KEY, lang);
    } catch {
      /* storage unavailable (private mode) — keep in-memory only */
    }
    this.applyHtmlLang(lang);
  }

  t(key: TranslationKey, params?: Record<string, string | number>): string {
    const dict = DICTS[this.state()];
    let text: string = dict[key] ?? DICTS.pt[key] ?? key;
    if (params) {
      for (const [name, value] of Object.entries(params)) {
        text = text.replaceAll(`{${name}}`, String(value));
      }
    }
    return text;
  }

  private applyHtmlLang(lang: AppLang): void {
    const htmlLang = LANGS.find((l) => l.id === lang)?.htmlLang ?? 'pt-BR';
    if (typeof document !== 'undefined') {
      document.documentElement.lang = htmlLang;
    }
  }
}

function detectInitialLang(): AppLang {
  try {
    const stored = localStorage.getItem(LANG_STORAGE_KEY);
    if (stored === 'pt' || stored === 'en' || stored === 'es') {
      return stored;
    }
  } catch {
    /* ignore */
  }
  if (typeof navigator !== 'undefined') {
    const nav = navigator.language.toLowerCase();
    if (nav.startsWith('es')) return 'es';
    if (nav.startsWith('en')) return 'en';
    if (nav.startsWith('pt')) return 'pt';
  }
  return 'pt';
}
