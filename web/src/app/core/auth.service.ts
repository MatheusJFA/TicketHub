import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, map, share, tap, throwError } from 'rxjs';
import { ConfigService } from './config.service';
import { SessionResponse } from './models';

interface JwtClaims {
  sub?: string;
  ownerId?: string;
  authorities?: string[];
  exp?: number;
}

const ACCESS_KEY = 'tickethub.accessToken';
const REFRESH_KEY = 'tickethub.refreshToken';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly config = inject(ConfigService);

  private readonly accessToken = signal<string | null>(localStorage.getItem(ACCESS_KEY));
  private readonly refreshToken = signal<string | null>(localStorage.getItem(REFRESH_KEY));
  private refreshInFlight: Observable<string> | null = null;

  readonly isLoggedIn = computed(() => this.accessToken() !== null);
  readonly claims = computed<JwtClaims | null>(() => decode(this.accessToken()));
  readonly customerId = computed(() => this.claims()?.ownerId ?? null);

  hasAuthority(authority: string): boolean {
    return this.claims()?.authorities?.includes(authority) ?? false;
  }

  get canManageCatalog(): boolean {
    return this.hasAuthority('show:create') || this.hasAuthority('show:write');
  }

  token(): string | null {
    return this.accessToken();
  }

  login(identifier: string, password: string) {
    return this.http
      .post<SessionResponse>(`${this.config.url()}/auth/login`, { identifier, password })
      .pipe(tap((session) => this.store(session)));
  }

  signup(cpf: string, name: string, email: string, password: string) {
    return this.http.post(`${this.config.url()}/customers`, { cpf, name, email, password });
  }

  logout() {
    const refresh = this.refreshToken();
    this.clear();
    this.router.navigate(['/login']);
    if (refresh) {
      this.http.post(`${this.config.url()}/auth/logout`, { refreshToken: refresh }).subscribe({
        error: () => undefined,
      });
    }
  }

  /**
   * Rotates the session using the stored refresh token. Concurrent callers
   * share a single in-flight request; failure clears the session.
   */
  refreshAccessToken(): Observable<string> {
    if (!this.refreshInFlight) {
      const refresh = this.refreshToken();
      if (!refresh) {
        return throwError(() => new Error('no refresh token'));
      }
      this.refreshInFlight = this.http
        .post<SessionResponse>(`${this.config.url()}/auth/refresh`, { refreshToken: refresh })
        .pipe(
          tap((session) => this.store(session)),
          map((session) => session.accessToken),
          catchError((err) => {
            this.clear();
            this.router.navigate(['/login']);
            return throwError(() => err);
          }),
          finalize(() => {
            this.refreshInFlight = null;
          }),
          share(),
        );
    }
    return this.refreshInFlight;
  }

  private store(session: SessionResponse): void {
    localStorage.setItem(ACCESS_KEY, session.accessToken);
    localStorage.setItem(REFRESH_KEY, session.refreshToken);
    this.accessToken.set(session.accessToken);
    this.refreshToken.set(session.refreshToken);
  }

  private clear(): void {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
    this.accessToken.set(null);
    this.refreshToken.set(null);
  }
}

function decode(token: string | null): JwtClaims | null {
  if (!token) {
    return null;
  }
  try {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
  } catch {
    return null;
  }
}
