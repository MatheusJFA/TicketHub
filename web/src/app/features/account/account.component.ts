import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountService } from '../../core/account.service';
import { AuthService } from '../../core/auth.service';
import { ConfirmService } from '../../core/confirm.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { parseApiError } from '../../core/api-error';
import { IconComponent } from '../../core/icon.component';

@Component({
  selector: 'app-account',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, IconComponent],
  templateUrl: './account.component.html',
})
export class AccountComponent {
  readonly auth = inject(AuthService);
  readonly i18n = inject(I18nService);
  private readonly account = inject(AccountService);
  private readonly confirm = inject(ConfirmService);
  private readonly router = inject(Router);

  name = '';
  cpf = '';
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly deleting = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    const id = this.auth.customerId();
    if (!id) {
      this.loading.set(false);
      return;
    }
    this.account.get(id).subscribe({
      next: (profile) => {
        this.name = profile.name;
        this.cpf = profile.cpf;
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  save(): void {
    const id = this.auth.customerId();
    if (!id) return;
    this.saving.set(true);
    this.saved.set(false);
    this.error.set(null);
    this.account.rename(id, this.name.trim()).subscribe({
      next: () => {
        this.saving.set(false);
        this.saved.set(true);
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
      },
    });
  }

  async remove(): Promise<void> {
    const id = this.auth.customerId();
    if (!id) return;
    if (!(await this.confirm.ask(this.i18n.t('account.confirmDelete')))) return;
    this.deleting.set(true);
    this.account.remove(id).subscribe({
      next: () => this.auth.logout(),
      error: (err) => {
        this.deleting.set(false);
        this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
      },
    });
  }
}
