import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { CartService } from './core/cart.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
})
export class App {
  readonly auth = inject(AuthService);
  readonly cart = inject(CartService);

  logout(): void {
    this.auth.logout();
  }
}
