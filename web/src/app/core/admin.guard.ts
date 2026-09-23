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
