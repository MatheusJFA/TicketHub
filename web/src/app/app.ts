import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './auth/auth.service';
import { CartService } from './orders/cart.service';
import { ConfirmDialogComponent } from './shared/ui/confirm-dialog.component';
import { IconComponent } from './shared/ui/icon.component';
import { ToastsComponent } from './shared/ui/toasts.component';
import { I18nService } from './shared/i18n/i18n.service';
import { LanguageSwitcherComponent } from './shared/ui/language-switcher.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LanguageSwitcherComponent, ToastsComponent, ConfirmDialogComponent, IconComponent],
  templateUrl: './app.html',
})
export class App {
  readonly auth = inject(AuthService);
  readonly cart = inject(CartService);
  readonly i18n = inject(I18nService);

  logout(): void {
    this.auth.logout();
    this.cart.clear();
  }
}
