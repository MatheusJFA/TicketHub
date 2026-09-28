import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, take, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { ConfigService } from './config.service';

const SKIPPED = ['/auth/login', '/auth/refresh', '/auth/logout'];

function isSameOrigin(url: string, apiBase: string): boolean {
  // Relativas ("/orders") são sempre API. Absolutas só com o base configurado.
  if (url.startsWith('/')) return true;
  if (apiBase && url.startsWith(apiBase)) return true;
  try {
    if (typeof location !== 'undefined' && !/^[a-z]+:\/\//i.test(url)) return true;
  } catch {
    // ignora
  }
  return false;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const config = inject(ConfigService);
  const skipped = SKIPPED.some((path) => req.url.includes(path));
  const token = auth.token();
  const outgoing =
    !token || skipped || !isSameOrigin(req.url, config.url())
      ? req
      : req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  return next(outgoing).pipe(
    catchError((err) => {
      if (err?.status !== 401 || skipped) {
        return throwError(() => err);
      }
      return auth.refreshAccessToken().pipe(
        take(1),
        switchMap((fresh) =>
          next(req.clone({ setHeaders: { Authorization: `Bearer ${fresh}` } })),
        ),
      );
    }),
  );
};
