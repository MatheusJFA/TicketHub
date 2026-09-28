import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';

interface AppConfig {
  apiUrl?: string;
  /**
   * Shows the demo-account box on the sign-in page. DEV only —
   * production deployments must omit this key (defaults to hidden).
   */
  showDemoAccounts?: boolean;
}

/**
 * Runtime configuration loaded from assets before bootstrap, so the same
 * Docker image talks to any backend without rebuild. Falls back to the
 * build-time environment when the file is missing.
 */
@Injectable({ providedIn: 'root' })
export class ConfigService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = signal(environment.apiUrl);
  private readonly demoAccounts = signal(false);

  url(): string {
    return this.apiUrl();
  }

  /** True only when the runtime config explicitly enables demo accounts. */
  showDemoAccounts(): boolean {
    return this.demoAccounts();
  }

  load() {
    return this.http.get<AppConfig>('/app-config.json').pipe(
      tap((config) => {
        if (config?.apiUrl) {
          this.apiUrl.set(config.apiUrl);
        }
        this.demoAccounts.set(config?.showDemoAccounts === true);
      }),
      map(() => undefined),
      catchError(() => of(undefined)),
    );
  }
}
