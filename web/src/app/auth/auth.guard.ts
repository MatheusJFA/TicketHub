import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  if (inject(AuthService).isLoggedIn()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/** Customer-only area (e.g. my tickets): requires order:write, which partners lack. */
export const customerGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  if (auth.isLoggedIn() && auth.customerId() && auth.canBuyTickets) {
    return true;
  }
  if (!auth.isLoggedIn()) {
    return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }
  return inject(Router).createUrlTree(['/']);
};
