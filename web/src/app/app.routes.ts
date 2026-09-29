import { Routes } from '@angular/router';
import { authGuard, customerGuard } from './auth/auth.guard';
import { adminGuard, gateGuard, masterGuard } from './auth/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/shows/shows-list.component').then((m) => m.ShowsListComponent),
    title: 'TicketHub · Shows',
  },
  {
    path: 'shows/:id',
    loadComponent: () =>
      import('./features/shows/show-detail.component').then((m) => m.ShowDetailComponent),
    title: 'TicketHub · Show',
  },
  {
    path: 'shows/:id/seats',
    loadComponent: () =>
      import('./features/seats/seat-map.component').then((m) => m.SeatMapComponent),
    title: 'TicketHub · Assentos',
  },
  {
    path: 'checkout',
    loadComponent: () =>
      import('./features/checkout/checkout.component').then((m) => m.CheckoutComponent),
    canActivate: [authGuard],
    title: 'TicketHub · Checkout',
  },
  {
    path: 'orders/:id',
    loadComponent: () =>
      import('./features/orders/order-status.component').then((m) => m.OrderStatusComponent),
    canActivate: [authGuard],
    title: 'TicketHub · Pedido',
  },
  {
    path: 'orders',
    loadComponent: () =>
      import('./features/orders/order-status.component').then((m) => m.OrderStatusComponent),
    canActivate: [authGuard],
    title: 'TicketHub · Pedidos',
  },
  {
    path: 'tickets',
    loadComponent: () =>
      import('./features/tickets/my-tickets.component').then((m) => m.MyTicketsComponent),
    canActivate: [customerGuard],
    title: 'TicketHub · Ingressos',
  },
  {
    path: 'check-in',
    loadComponent: () => import('./features/check-in/check-in.component').then((m) => m.CheckInComponent),
    canActivate: [gateGuard],
    title: 'TicketHub · Checagem',
  },
  { path: 'gate', redirectTo: 'check-in' },
  {
    path: 'partner',
    loadComponent: () =>
      import('./features/partner/partner.component').then((m) => m.PartnerComponent),
    title: 'TicketHub · Parceiro',
  },
  {
    path: 'admin/coupons',
    loadComponent: () =>
      import('./features/admin/admin-coupons.component').then((m) => m.AdminCouponsComponent),
    canActivate: [masterGuard],
    title: 'TicketHub · Cupons',
  },
  {
    path: 'admin/partners',
    loadComponent: () =>
      import('./features/admin/admin-partners.component').then((m) => m.AdminPartnersComponent),
    canActivate: [masterGuard],
    title: 'TicketHub · Parceiros',
  },
  {
    path: 'admin/operators',
    loadComponent: () =>
      import('./features/admin/admin-operators.component').then((m) => m.AdminOperatorsComponent),
    canActivate: [masterGuard],
    title: 'TicketHub · Operadores',
  },
  {
    path: 'admin/audit',
    loadComponent: () =>
      import('./features/admin/admin-audit.component').then((m) => m.AdminAuditComponent),
    canActivate: [masterGuard],
    title: 'TicketHub · Auditoria',
  },
  {
    path: 'account',
    loadComponent: () =>
      import('./features/account/account.component').then((m) => m.AccountComponent),
    canActivate: [authGuard],
    title: 'TicketHub · Conta',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login.component').then((m) => m.LoginComponent),
    title: 'TicketHub · Entrar',
  },
  {
    path: 'admin',
    loadComponent: () =>
      import('./features/admin/admin-shows.component').then((m) => m.AdminShowsComponent),
    canActivate: [adminGuard],
    title: 'TicketHub · Admin',
  },
  {
    path: 'admin/shows/:id',
    loadComponent: () =>
      import('./features/admin/admin-show-detail.component').then((m) => m.AdminShowDetailComponent),
    canActivate: [adminGuard],
    title: 'TicketHub · Mapa',
  },
  {
    path: 'signup',
    loadComponent: () =>
      import('./features/auth/signup.component').then((m) => m.SignupComponent),
    title: 'TicketHub · Cadastro',
  },
  {
    path: 'not-found',
    loadComponent: () =>
      import('./features/shows/shows-list.component').then((m) => m.ShowsListComponent),
    title: 'TicketHub · Não encontrado',
  },
  { path: '**', redirectTo: 'not-found' },
];
