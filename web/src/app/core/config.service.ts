import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';

interface AppConfig {
  apiUrl?: string;
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

  url(): string {
    return this.apiUrl();
  }

  load() {
    return this.http.get<AppConfig>('/app-config.json').pipe(
      tap((config) => {
        if (config?.apiUrl) {
          this.apiUrl.set(config.apiUrl);
        }
      }),
      map(() => undefined),
      catchError(() => of(undefined)),
    );
  }
}
