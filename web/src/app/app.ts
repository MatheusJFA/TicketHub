import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { CartService } from './core/cart.service';
import { ConfirmDialogComponent } from './core/confirm-dialog.component';
import { IconComponent } from './core/icon.component';
import { ToastsComponent } from './core/toasts.component';
import { I18nService } from './core/i18n/i18n.service';
import { LanguageSwitcherComponent } from './core/i18n/language-switcher.component';

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
