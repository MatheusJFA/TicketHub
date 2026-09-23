import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/shows/shows-list.component').then((m) => m.ShowsListComponent),
  },
  {
    path: 'shows/:id',
    loadComponent: () =>
      import('./features/shows/show-detail.component').then((m) => m.ShowDetailComponent),
  },
  {
    path: 'shows/:id/seats',
    loadComponent: () =>
      import('./features/seats/seat-map.component').then((m) => m.SeatMapComponent),
  },
  {
    path: 'seats',
    loadComponent: () =>
      import('./features/seats/seats.component').then((m) => m.SeatsComponent),
  },
  {
    path: 'checkout',
    loadComponent: () =>
      import('./features/checkout/checkout.component').then((m) => m.CheckoutComponent),
    canActivate: [authGuard],
  },
  {
    path: 'orders/:id',
    loadComponent: () =>
      import('./features/orders/order-status.component').then((m) => m.OrderStatusComponent),
    canActivate: [authGuard],
  },
  {
    path: 'orders',
    loadComponent: () =>
      import('./features/orders/order-status.component').then((m) => m.OrderStatusComponent),
    canActivate: [authGuard],
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'signup',
    loadComponent: () =>
      import('./features/auth/signup.component').then((m) => m.SignupComponent),
  },
  { path: '**', redirectTo: '' },
];
