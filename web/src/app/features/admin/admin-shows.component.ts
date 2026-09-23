import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CatalogService } from '../../core/catalog.service';
import { AdminService } from '../../core/admin.service';
import { AuthService } from '../../core/auth.service';
import { ShowSummary } from '../../core/models';

@Component({
  selector: 'app-admin-shows',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-shows.component.html',
})
export class AdminShowsComponent {
  private readonly catalog = inject(CatalogService);
  private readonly admin = inject(AdminService);
  private readonly auth = inject(AuthService);

  readonly shows = signal<ShowSummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly creating = signal(false);

  partnerId = this.auth.customerId() ?? '';
  name = '';
  description = '';
  date = '';
  city = '';
  state = '';

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.catalog.listShows('', 0, 50).subscribe({
      next: (page) => {
        this.shows.set(page.items);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar os shows.');
        this.loading.set(false);
      },
    });
  }

  publish(id: string): void {
    this.admin.publishShow(id).subscribe({
      next: () => this.reload(),
      error: (err) => this.error.set(err?.error?.errors?.[0]?.message ?? 'Falha ao publicar.'),
    });
  }

  create(): void {
    this.error.set(null);
    this.creating.set(true);
    this.admin
      .createShow({
        partnerId: this.partnerId,
        name: this.name,
        description: this.description,
        date: this.date,
        address: {
          street: 'Rua a definir',
          number: 's/n',
          complement: null,
          neighborhood: 'Centro',
          city: this.city,
          state: this.state,
          country: 'Brasil',
          zipCode: '00000-000',
        },
      })
      .subscribe({
        next: () => {
          this.creating.set(false);
          this.name = '';
          this.description = '';
          this.date = '';
          this.reload();
        },
        error: (err) => {
          this.creating.set(false);
          this.error.set(err?.error?.errors?.[0]?.message ?? 'Falha ao criar show.');
        },
      });
  }
}
