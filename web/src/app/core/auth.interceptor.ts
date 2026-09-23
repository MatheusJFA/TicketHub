import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const SKIPPED = ['/auth/login', '/auth/refresh', '/auth/logout'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const outgoing =
    !token || SKIPPED.some((path) => req.url.includes(path))
      ? req
      : req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  return next(outgoing).pipe(
    catchError((err) => {
      if (err?.status !== 401 || SKIPPED.some((path) => req.url.includes(path))) {
        return throwError(() => err);
      }
      return auth.refreshAccessToken().pipe(
        switchMap((fresh) =>
          next(req.clone({ setHeaders: { Authorization: `Bearer ${fresh}` } })),
        ),
      );
    }),
  );
};
