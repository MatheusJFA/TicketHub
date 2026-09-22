import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';
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

  private readonly accessToken = signal<string | null>(localStorage.getItem(ACCESS_KEY));
  private readonly refreshToken = signal<string | null>(localStorage.getItem(REFRESH_KEY));

  readonly isLoggedIn = computed(() => this.accessToken() !== null);
  readonly claims = computed<JwtClaims | null>(() => decode(this.accessToken()));
  readonly customerId = computed(() => this.claims()?.ownerId ?? null);

  token(): string | null {
    return this.accessToken();
  }

  login(identifier: string, password: string) {
    return this.http
      .post<SessionResponse>(`${environment.apiUrl}/auth/login`, { identifier, password })
      .pipe(tap((session) => this.store(session)));
  }

  signup(cpf: string, name: string, email: string, password: string) {
    return this.http.post(`${environment.apiUrl}/customers`, { cpf, name, email, password });
  }

  logout() {
    const refresh = this.refreshToken();
    this.clear();
    this.router.navigate(['/login']);
    if (refresh) {
      this.http.post(`${environment.apiUrl}/auth/logout`, { refreshToken: refresh }).subscribe({
        error: () => undefined,
      });
    }
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
