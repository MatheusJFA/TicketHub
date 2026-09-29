import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (auth.isLoggedIn() && auth.canManageCatalog) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: '/admin' } });
};

/** Master-only area (e.g. coupons): requires the ADMIN role. */
export const masterGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  if (auth.isLoggedIn() && auth.isAdmin) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/** Check-in control: partners (or admins) holding ticket:validate. */
export const gateGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  if (auth.isLoggedIn() && auth.canValidateTickets) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
